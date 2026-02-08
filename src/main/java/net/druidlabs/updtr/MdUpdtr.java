package net.druidlabs.updtr;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import io.github.andruid929.leutils.time.TaskTimer;
import io.github.andruid929.leutils.time.TimeUnitConversion;
import net.druidlabs.updtr.api.Request;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MdUpdtr {

    private static Session session;

    private static Map<Mod, String> downloadQueue = new ConcurrentHashMap<>();

    private static Set<Mod> localMods;

    public static void main(String[] args) throws InterruptedException {
        ErrorLogger.initiate();

        try {
            session = Session.createSession();

            System.out.println(session.getGameVersion());
            System.out.println(session.getModLoader().getName());

            {
                Thread.Builder.OfVirtual localModThread = Thread.ofVirtual().name("local-mod-worker");

                Runnable installedModsTask = () -> {
                    try {
                        localMods = InOut.loadLocalMods(null);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };

                localModThread.start(installedModsTask);
            }

            Set<Mod> unmappedMods = checkForUpdates(localMods);

            if (!unmappedMods.isEmpty()) {
                //Ask the user to provide unmapped mod URLs

                requestMappings(null);
            }



        } catch (Exception e) {
            ErrorLogger.logError(e);

            Thread.sleep(2000);
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

                    try (Request request = GetModRequest.requestForMod(modProjectId)) {

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
                        } else {
                            //Add update to download queue


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

                    try (Request request = SearchModRequest.searchMod(Constants.MINECRAFT_GAME_ID_SLUG, slug)) {

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
