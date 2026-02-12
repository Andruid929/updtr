package net.druidlabs.updtr;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import io.github.andruid929.leutils.time.TaskTimer;
import io.github.andruid929.leutils.time.TimeUnitConversion;
import net.druidlabs.updtr.api.Request;
import net.druidlabs.updtr.api.requests.GetModFileRequest;
import net.druidlabs.updtr.api.requests.GetModRequest;
import net.druidlabs.updtr.api.requests.RequestException;
import net.druidlabs.updtr.api.requests.SearchModRequest;
import net.druidlabs.updtr.api.response.FileIndex;
import net.druidlabs.updtr.api.response.ResponseHandler;
import net.druidlabs.updtr.errorhandling.ErrorLogger;
import net.druidlabs.updtr.io.InOut;
import net.druidlabs.updtr.mods.CFMod;
import net.druidlabs.updtr.mods.Mappings;
import net.druidlabs.updtr.mods.Mod;
import net.druidlabs.updtr.session.Session;
import net.druidlabs.updtr.util.SlugExtractor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MdUpdtr {

    private static Session session;

    private static final Map<Mod, String> downloadQueue = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {
        ErrorLogger.initiate();

        try {
            session = Session.createSession();

            System.out.println(session.getGameVersion());
            System.out.println(session.getModLoader().getName());

            Set<Mod> localMods = InOut.loadLocalMods(null);

            Set<Mod> unmappedMods = checkForUpdates(localMods);

            if (!unmappedMods.isEmpty()) {
                //Ask the user to provide unmapped mod URLs
                System.out.println("Mapps");

            } else {
                InOut.backupMods();

                downloadUpdates();
            }


        } catch (Exception e) {
            ErrorLogger.logError(e);

        } finally {
            Thread.sleep(2000);
        }
    }

    private static void downloadUpdates() throws InterruptedException {
        Semaphore semaphore = new Semaphore(4);

        ExecutorService downloadQueueService = Executors.newVirtualThreadPerTaskExecutor();

        try {
            InOut.backupMods();

            for (Mod mod : downloadQueue.keySet()) {

                downloadQueueService.submit(() -> {
                    String downloadUrl = downloadQueue.get(mod);

                    try {
                        semaphore.acquire();

                        if (InOut.updateFile(mod, downloadUrl)) {
                            System.out.println("Done downloading update for " + mod.getModName());
                        } else {
                            System.out.println("Failed to download update for " + mod.getModName());
                        }
                    } catch (InterruptedException | IOException e) {
                        ErrorLogger.logError(e);

                        Thread.currentThread().interrupt();
                    } finally {
                        semaphore.release();
                    }

                });
            }
        } catch (Exception e) {
            ErrorLogger.logError(e);

        } finally {
            downloadQueueService.shutdown();
            downloadQueueService.awaitTermination(1, TimeUnit.MINUTES);
        }
    }

    private static @NotNull Set<Mod> checkForUpdates(@NotNull Set<Mod> localMods) {
        Set<Mod> unmappedMods = ConcurrentHashMap.newKeySet();

        try (ExecutorService service = Executors.newVirtualThreadPerTaskExecutor()) {

            for (Mod mod : localMods) {
                CFMod mapping = Mappings.getMapping(mod);

                if (mapping == null) {
                    unmappedMods.add(mod);

                    continue;
                }

                service.submit(() -> {
                    int modProjectId = mapping.getProjectId();

                    String threadName = mod.getModId() + "-UpdateThread";

                    Thread currentThread = Thread.currentThread();
                    currentThread.setName(threadName);

                    try {
                        Request request = GetModRequest.requestForMod(modProjectId);

                        int responseCode = request.getResponseCode();

                        if (responseCode != 200) {
                            throw new IllegalStateException(String.valueOf(responseCode));
                        }

                        ResponseHandler rHandler = ResponseHandler.handleGetModResponse(request.getResponse());

                        JsonArray indexArray = rHandler.asJsonArray(ResponseHandler.LATEST_FILES_INDEXES);

                        Type indexType = new TypeToken<List<FileIndex>>() {
                        }.getType();

                        List<FileIndex> allIndexes = new Gson().fromJson(indexArray, indexType);

                        List<FileIndex> currentVersionIndexes = allIndexes.stream()
                                .filter(fileIndex -> fileIndex.getGameVersion().equals(session.getGameVersion()))
                                .toList();

                        FileIndex lastestFileIndex = currentVersionIndexes.getFirst();

                        if (lastestFileIndex.getFilename().equals(mod.getModFileName())) {
                            //No updates found
                            System.out.println("No updates found");

                            return;
                        }

                        //Add update to download queue
                        int fileId = lastestFileIndex.getFileId();

                        Request getModUpdateFileUrl = GetModFileRequest.getModFileUrl(modProjectId, fileId);

                        if (getModUpdateFileUrl.getResponseCode() == 200) {
                            ResponseHandler responseHandler = ResponseHandler.handleGetFileUrlResponse(getModUpdateFileUrl.getResponse());

                            String url = responseHandler.asString("data");

                            downloadQueue.put(mod, url);
                        }
                    } catch (Exception e) {
                        ErrorLogger.logError(new RequestException(e, threadName.concat(" failed to finish")));

                        currentThread.interrupt();
                    }
                });
            }
        }

        return unmappedMods;
    }

    @Contract("_ -> param1")
    private static @NotNull ConcurrentHashMap<Mod, String> requestMappings(@NotNull ConcurrentHashMap<Mod, String> modUrlMap) {
        int initialTasks = modUrlMap.size();

        if (initialTasks == 0) {
            return modUrlMap;
        }

        try (ExecutorService service = Executors.newVirtualThreadPerTaskExecutor()) {

            AtomicInteger finishedTasks = new AtomicInteger(0);

            TaskTimer timer = new TaskTimer();

            for (Mod mod : modUrlMap.keySet()) {
                String url = modUrlMap.get(mod);

                String slug = "slug=".concat(SlugExtractor.getSlug(url));

                service.submit(() -> {
                    String threadName = mod.getModId().concat("-Request-Thread");

                    Thread.currentThread().setName(threadName);

                    try {
                        Request request = SearchModRequest.searchMod(Constants.MINECRAFT_GAME_ID_PARAM, slug);

                        int projectId;

                        if (request.getResponseCode() == 200) {
                            ResponseHandler rHandler = ResponseHandler.handleSearchModResponse(request.getResponse());

                            projectId = rHandler.getModProjectId();

                        } else {
                            projectId = -1;
                        }

                        Mappings.addMappingEntry(mod, projectId);

                        modUrlMap.remove(mod);

                        finishedTasks.incrementAndGet();

                    } catch (Exception e) {
                        String failMessage = Thread.currentThread().getName() + " failed to finish";

                        ErrorLogger.logError(new RequestException(e, failMessage));
                    }
                });
            }

            Mappings.persistMappings();

            String timeTakenInS = timer.formatElapsedTime(TimeUnitConversion.Unit.SECONDS);

            System.out.println("Successfully mapped " + finishedTasks.get() + "/" + initialTasks + " mods in ".concat(timeTakenInS));
        }

        return modUrlMap;
    }
}
