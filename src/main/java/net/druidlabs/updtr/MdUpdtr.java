package net.druidlabs.updtr;

import io.github.andruid929.leutils.time.TaskTimer;
import io.github.andruid929.leutils.time.TimeUnitConversion;
import net.druidlabs.updtr.api.Request;
import net.druidlabs.updtr.api.requests.RequestException;
import net.druidlabs.updtr.api.requests.SearchModRequest;
import net.druidlabs.updtr.errorhandling.ErrorLogger;
import net.druidlabs.updtr.mods.Mapping;
import net.druidlabs.updtr.mods.Mod;
import net.druidlabs.updtr.session.Session;
import net.druidlabs.updtr.util.ResponseHandler;
import net.druidlabs.updtr.util.SlugExtractor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MdUpdtr {

    public static void main(String[] args) throws InterruptedException {
        ErrorLogger.initiate();

        try {
            Session session = Session.createSession();

            System.out.println(session.getGameVersion());
            System.out.println(session.getModLoader().getName());

            Mapping.updateMappings();

        } catch (Exception e) {
            ErrorLogger.logError(e);

            Thread.sleep(2000);
        }
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

                        Mapping.addMappingEntry(mod, projectId);

                        modUrlMap.remove(mod);

                        finishedTasks.incrementAndGet();

                    } catch (Exception e) {
                        String failMessage = Thread.currentThread().getName() + " failed to finish";

                        ErrorLogger.logError(new RequestException(e, failMessage));
                    }
                });
            }

            Mapping.persistMappings();

            String timeTakenInS = timer.formatElapsedTime(TimeUnitConversion.Unit.SECONDS);

            System.out.println("Successfully mapped " + finishedTasks.get() + "/" + initialTasks + " mods in ".concat(timeTakenInS));
        }

        return modUrlMap;
    }
}
