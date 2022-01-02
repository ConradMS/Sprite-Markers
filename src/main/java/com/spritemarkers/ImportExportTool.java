package com.spritemarkers;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.menus.WidgetMenuOption;

import javax.inject.Inject;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ImportExportTool
{
    private static final WidgetMenuOption EXPORT_SPRITE_MARKERS = new WidgetMenuOption("Export", "Sprite Markers", WidgetInfo.MINIMAP_TOGGLE_RUN_ORB);
    private static final WidgetMenuOption IMPORT_SPRITE_MARKERS = new WidgetMenuOption("Import", "Sprite Markers", WidgetInfo.MINIMAP_TOGGLE_RUN_ORB);

    private final SpriteMarkersPlugin spriteMarkersPlugin;
    private final Client client;
    private final MenuManager menuManager;
    private final ChatMessageManager chatMessageManager;
    private final Gson gson;

    @Inject
    private ImportExportTool(SpriteMarkersPlugin plugin, Client client, ChatMessageManager chatMessageManager, MenuManager menuManager, Gson gson)
    {
        this.spriteMarkersPlugin = plugin;
        this.client = client;
        this.menuManager = menuManager;
        this.chatMessageManager = chatMessageManager;
        this.gson = gson;
    }

    void addOptions()
    {
        menuManager.addManagedCustomMenu(EXPORT_SPRITE_MARKERS, this::exportSpriteMarkers);
        menuManager.addManagedCustomMenu(IMPORT_SPRITE_MARKERS, this::importSpriteMarkers);
    }

    void removeOptions()
    {
        menuManager.removeManagedCustomMenu(EXPORT_SPRITE_MARKERS);
        menuManager.removeManagedCustomMenu(IMPORT_SPRITE_MARKERS);
    }

    private void exportSpriteMarkers(MenuEntry menuEntry)
    {
        final ArrayList<SpriteMarker> spriteMarkersLoaded = spriteMarkersPlugin.getSpriteMarkersLoaded();
        if (spriteMarkersLoaded.isEmpty())
        {
            addChatMessage("There are no loaded sprite markers to export");
            return;
        }

        final int[] loadedRegions = client.getMapRegions();
        if(loadedRegions == null || loadedRegions.length == 0)
        {
            return;
        }

        ArrayList<SpriteMarkerID> spriteIDSToSave = new ArrayList<>();
        for(int region : loadedRegions)
        {
            spriteIDSToSave.addAll(spriteMarkersPlugin.jsonToSprite(region));
        }

        String markersJSONString = this.gson.toJson(spriteIDSToSave);

        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(markersJSONString), null);
        addChatMessage("Copied loaded sprite markers to clipboard");
    }

    private void importSpriteMarkers(MenuEntry menuEntry)
    {
        final String text;
        try
        {
           text = Toolkit.getDefaultToolkit().getSystemClipboard()
                   .getData(DataFlavor.stringFlavor)
                   .toString();
        }
        catch (IOException | UnsupportedFlavorException e)
        {
            addChatMessage("Clipboard error - could not import sprite markers");
            return;
        }

        if(text.isEmpty() || text == null)
        {
            addChatMessage("Empty clipboard - could not import sprite markers");
            return;
        }

        ArrayList<SpriteMarkerID> spriteMarkerIDS = new ArrayList<>();

        try
        {
            spriteMarkerIDS = gson.fromJson(text, new TypeToken<ArrayList<SpriteMarkerID>>(){}.getType());
        }catch (JsonSyntaxException e)
        {
            addChatMessage("No Sprites in clipboard/Wrong formatting - could not import sprite markers");
            return;
        }

        if (spriteMarkerIDS == null || spriteMarkerIDS.isEmpty())
        {
            addChatMessage("No Sprite Markers in clipboard");
            return;
        }

        addToConfig(spriteMarkerIDS);
    }

    private void addToConfig(ArrayList<SpriteMarkerID> spriteMarkerIDS)
    {
        HashMap<Integer, ArrayList<SpriteMarkerID>> IDToMarkers = new HashMap<>();
        int currRegion;
        for(SpriteMarkerID spriteMarkerID : spriteMarkerIDS)
        {
            currRegion = spriteMarkerID.getRegionId();
            if(IDToMarkers.containsKey(currRegion))
            {
                IDToMarkers.get(currRegion).add(spriteMarkerID);
            }
            else
            {
                IDToMarkers.put(currRegion, new ArrayList<>(Collections.singletonList(spriteMarkerID)));
            }
        }

        for(int regionId : IDToMarkers.keySet())
        {
            spriteMarkersPlugin.saveSprite(regionId, IDToMarkers.get(regionId));
        }
        spriteMarkersPlugin.loadSprites();
        addChatMessage("Sprite Markers imported");
    }

    private void addChatMessage(String message)
    {
        chatMessageManager.queue(QueuedMessage.builder()
                .type(ChatMessageType.CONSOLE)
                .runeLiteFormattedMessage(message)
                .build()
        );
    }
}
