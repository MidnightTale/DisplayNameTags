package com.mattmx.nametags.entity.trait;

import com.mattmx.nametags.entity.NameTagEntity;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import io.github.retrooper.packetevents.util.folia.TaskWrapper;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RefreshTrait extends Trait {
    private @Nullable TaskWrapper task = null;
    private final JavaPlugin plugin;
    private final long period;
    private final TimeUnit unit;
    private final Consumer<NameTagEntity> update;
    private boolean paused = false;

    public RefreshTrait(@NotNull JavaPlugin plugin, long period, TimeUnit unit, Consumer<NameTagEntity> update) {
        this.plugin = plugin;
        this.period = period;
        this.unit = unit;
        this.update = update;
    }

    @Override
    public void onEnable() {
        this.task = FoliaScheduler.getAsyncScheduler().runAtFixedRate(plugin, (task) -> {

                // Don't process if paused
                if (!this.isPaused()) {

                    // If the tag is not currently spawned in then we shouldn't process
                    if (!getTag().getPassenger().isSpawned()) {
                        return;
                    }

                    update.accept(getTag());
                }

            }, 1L, period, unit);
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return this.paused;
    }

    @Override
    public void onDestroy() {
        setPaused(true);
        if (task != null) {
            task.cancel();
        }
    }

    public static @NotNull RefreshTrait ofMinutes(@NotNull JavaPlugin plugin, long minutes, Consumer<NameTagEntity> update) {
        return new RefreshTrait(plugin, minutes, TimeUnit.MINUTES, update);
    }

    public static @NotNull RefreshTrait ofSeconds(@NotNull JavaPlugin plugin, long seconds, Consumer<NameTagEntity> update) {
        return new RefreshTrait(plugin, seconds, TimeUnit.SECONDS, update);
    }

    public static @NotNull RefreshTrait ofMillis(@NotNull JavaPlugin plugin, long millis, Consumer<NameTagEntity> update) {
        return new RefreshTrait(plugin, millis, TimeUnit.MILLISECONDS, update);
    }

    public static @NotNull RefreshTrait ofTicks(@NotNull JavaPlugin plugin, long ticks, Consumer<NameTagEntity> update) {
        return ofMillis(plugin, ticks * 50, update);
    }

}
