package its.lugoff.luxSB.schematics;

import its.lugoff.luxSB.LuxSB;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.EditSession;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SchematicLoader {

    public static boolean loadSchematic(String schematicName, Location center) {
        LuxSB plugin = LuxSB.getPlugin(LuxSB.class);
        File schematicFile = new File(plugin.getDataFolder(), "schematics/" + schematicName + ".schem");

        if (!schematicFile.exists()) {
            plugin.getLogger().warning("Schematic file not found: " + schematicFile.getAbsolutePath());
            return false;
        }

        try (FileInputStream fis = new FileInputStream(schematicFile)) {
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                plugin.getLogger().warning("Unknown schematic format for: " + schematicName);
                return false;
            }
            Clipboard clipboard = format.getReader(fis).read();
            ClipboardHolder holder = new ClipboardHolder(clipboard);
            WorldEdit worldEdit = WorldEdit.getInstance();

            com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(center.getWorld());
            try (EditSession editSession = worldEdit.newEditSession(adaptedWorld)) {
                Operation operation = holder.createPaste(editSession)
                        .to(BlockVector3.at(center.getX(), center.getY(), center.getZ()))
                        .ignoreAirBlocks(true)
                        .build();
                Operations.complete(operation);
                plugin.getLogger().info("Loaded schematic '" + schematicName + "' at " + center.toString());
                return true; // Success
            }
        } catch (IOException | com.sk89q.worldedit.WorldEditException e) {
            plugin.getLogger().severe("Failed to load schematic '" + schematicName + "': " + e.getMessage());
            e.printStackTrace();
            return false; // Failure
        }
    }
}