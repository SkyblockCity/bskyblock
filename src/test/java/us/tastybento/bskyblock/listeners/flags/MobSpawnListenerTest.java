package us.tastybento.bskyblock.listeners.flags;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.PluginManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import us.tastybento.bskyblock.BSkyBlock;
import us.tastybento.bskyblock.database.objects.Island;
import us.tastybento.bskyblock.generators.IslandWorld;
import us.tastybento.bskyblock.lists.Flags;
import us.tastybento.bskyblock.managers.FlagsManager;
import us.tastybento.bskyblock.managers.IslandsManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { Flags.class })
public class MobSpawnListenerTest {

    private static Location location;
    private static BSkyBlock plugin;
    private static FlagsManager flagsManager;
    private static Zombie zombie;
    private static Slime slime;
    private static Cow cow;

    @BeforeClass
    public static void setUp() {
        Server server = mock(Server.class);
        World world = mock(World.class);
        when(server.getLogger()).thenReturn(Logger.getAnonymousLogger());
        when(server.getWorld("world")).thenReturn(world);
        when(server.getVersion()).thenReturn("BSB_Mocking");

        PluginManager pluginManager = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(pluginManager);

        ItemFactory itemFactory = mock(ItemFactory.class);
        when(server.getItemFactory()).thenReturn(itemFactory);

        Bukkit.setServer(server);

        SkullMeta skullMeta = mock(SkullMeta.class);
        when(itemFactory.getItemMeta(any())).thenReturn(skullMeta);
        when(Bukkit.getItemFactory()).thenReturn(itemFactory);
        when(Bukkit.getLogger()).thenReturn(Logger.getAnonymousLogger());
        location = mock(Location.class);
        when(location.getWorld()).thenReturn(world);
        when(location.getBlockX()).thenReturn(0);
        when(location.getBlockY()).thenReturn(0);
        when(location.getBlockZ()).thenReturn(0);
        PowerMockito.mockStatic(Flags.class);

        plugin = Mockito.mock(BSkyBlock.class);
        flagsManager = new FlagsManager(plugin);
        when(plugin.getFlagsManager()).thenReturn(flagsManager);


        // Worlds
        IslandWorld iwm = mock(IslandWorld.class);
        when(plugin.getIslandWorldManager()).thenReturn(iwm);
        when(iwm.getIslandWorld()).thenReturn(world);
        when(iwm.getNetherWorld()).thenReturn(world);
        when(iwm.getEndWorld()).thenReturn(world);

        MobSpawnListener listener = mock(MobSpawnListener.class);        
        when(listener.inWorld(any(Location.class))).thenReturn(true);
        when(listener.inWorld(any(Entity.class))).thenReturn(true);

        // Monsters and animals
        zombie = mock(Zombie.class);
        when(zombie.getLocation()).thenReturn(location);
        slime = mock(Slime.class);
        when(slime.getLocation()).thenReturn(location);
        cow = mock(Cow.class);
        when(cow.getLocation()).thenReturn(location);

    }

   
    @Test
    public void testNotInWorld() {
        IslandsManager im = mock(IslandsManager.class);
        when(plugin.getIslands()).thenReturn(im);
        Island island = mock(Island.class);
        when(im.getIslandAt(Matchers.any())).thenReturn(Optional.of(island));

        // Set up entity
        LivingEntity entity = mock(LivingEntity.class);
        when(entity.getLocation()).thenReturn(null);
        
        // Setup event
        CreatureSpawnEvent e = mock(CreatureSpawnEvent.class);
        when(e.getLocation()).thenReturn(location);

        // Setup the listener
        MobSpawnListener l = new MobSpawnListener();
        l.setPlugin(plugin);

        // Check monsters
        when(e.getEntity()).thenReturn(entity);

        // Should not be canceled
        assertFalse(l.onNaturalMobSpawn(e));
    }
    
    @Test
    public void testOnNaturalMonsterSpawnBlocked() {
        IslandsManager im = mock(IslandsManager.class);
        when(plugin.getIslands()).thenReturn(im);
        Island island = mock(Island.class);
        when(im.getIslandAt(Matchers.any())).thenReturn(Optional.of(island));

        // Block mobs
        when(island.isAllowed(Mockito.any())).thenReturn(false);

        // Setup event
        CreatureSpawnEvent e = mock(CreatureSpawnEvent.class);
        when(e.getLocation()).thenReturn(location);

        // Setup the listener
        MobSpawnListener l = new MobSpawnListener();
        l.setPlugin(plugin);

        // Check monsters
        when(e.getEntity()).thenReturn(zombie);
        checkBlocked(e,l);
        when(e.getEntity()).thenReturn(slime);
        checkBlocked(e,l);
        // Check animal
        when(e.getEntity()).thenReturn(cow);
        checkBlocked(e,l);

    }

    private void checkBlocked(CreatureSpawnEvent e, MobSpawnListener l) {
        for (SpawnReason reason: SpawnReason.values()) {
            when(e.getSpawnReason()).thenReturn(reason);
            if (reason.equals(SpawnReason.NATURAL)
                    || reason.equals(SpawnReason.JOCKEY)
                    || reason.equals(SpawnReason.CHUNK_GEN)
                    || reason.equals(SpawnReason.DEFAULT)
                    || reason.equals(SpawnReason.MOUNT)
                    || reason.equals(SpawnReason.NETHER_PORTAL)) {
                assertTrue(l.onNaturalMobSpawn(e));
            } else {
                assertFalse(l.onNaturalMobSpawn(e));
            }
        }

    }

    @Test
    public void testOnNaturalMobSpawnUnBlocked() {
        IslandsManager im = mock(IslandsManager.class);
        when(plugin.getIslands()).thenReturn(im);
        Island island = mock(Island.class);
        when(im.getIslandAt(Matchers.any())).thenReturn(Optional.of(island));

        // Allow mobs
        when(island.isAllowed(Mockito.any())).thenReturn(true);

        // Setup event
        CreatureSpawnEvent e = mock(CreatureSpawnEvent.class);
        when(e.getLocation()).thenReturn(location);

        // Setup the listener
        MobSpawnListener l = new MobSpawnListener();
        l.setPlugin(plugin);

        // Check monsters
        when(e.getEntity()).thenReturn(zombie);
        checkUnBlocked(e,l);
        when(e.getEntity()).thenReturn(slime);
        checkUnBlocked(e,l);
        // Check animal
        when(e.getEntity()).thenReturn(cow);
        checkUnBlocked(e,l);

    }

    private void checkUnBlocked(CreatureSpawnEvent e, MobSpawnListener l) {
        for (SpawnReason reason: SpawnReason.values()) {
            when(e.getSpawnReason()).thenReturn(reason);
            assertFalse(l.onNaturalMobSpawn(e));
        }

    }
    
    @Test
    public void testOnNaturalMonsterSpawnBlockedNoIsland() {
        IslandsManager im = mock(IslandsManager.class);
        when(plugin.getIslands()).thenReturn(im);
        when(im.getIslandAt(Matchers.any())).thenReturn(Optional.empty());

        // Block mobs
        Flags.MONSTER_SPAWN.setDefaultSetting(false);
        Flags.ANIMAL_SPAWN.setDefaultSetting(false);
        // Setup event
        CreatureSpawnEvent e = mock(CreatureSpawnEvent.class);
        when(e.getLocation()).thenReturn(location);

        // Setup the listener
        MobSpawnListener l = new MobSpawnListener();
        l.setPlugin(plugin);

        // Check monsters
        when(e.getEntity()).thenReturn(zombie);
        checkBlocked(e,l);
        when(e.getEntity()).thenReturn(slime);
        checkBlocked(e,l);
        // Check animal
        when(e.getEntity()).thenReturn(cow);
        checkBlocked(e,l);

    }
    
    @Test
    public void testOnNaturalMobSpawnUnBlockedNoIsland() {
        IslandsManager im = mock(IslandsManager.class);
        when(plugin.getIslands()).thenReturn(im);
        when(im.getIslandAt(Matchers.any())).thenReturn(Optional.empty());

        // Block mobs
        Flags.MONSTER_SPAWN.setDefaultSetting(true);
        Flags.ANIMAL_SPAWN.setDefaultSetting(true);
        
        // Setup event
        CreatureSpawnEvent e = mock(CreatureSpawnEvent.class);
        when(e.getLocation()).thenReturn(location);

        // Setup the listener
        MobSpawnListener l = new MobSpawnListener();
        l.setPlugin(plugin);

        // Check monsters
        when(e.getEntity()).thenReturn(zombie);
        checkUnBlocked(e,l);
        when(e.getEntity()).thenReturn(slime);
        checkUnBlocked(e,l);
        // Check animal
        when(e.getEntity()).thenReturn(cow);
        checkUnBlocked(e,l);

    }

}
