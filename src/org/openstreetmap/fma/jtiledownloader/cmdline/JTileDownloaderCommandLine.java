/*
 * Copyright 2008, Friedrich Maier
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
 * 
 * This file is part of JTileDownloader.
 * (see http://wiki.openstreetmap.org/index.php/JTileDownloader)
 *
 * JTileDownloader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JTileDownloader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy (see file COPYING.txt) of the GNU 
 * General Public License along with JTileDownloader.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.openstreetmap.fma.jtiledownloader.cmdline;

import java.util.HashMap;
import java.util.Vector;

import org.openstreetmap.fma.jtiledownloader.TileListDownloader;
import org.openstreetmap.fma.jtiledownloader.config.DownloadConfiguration;
import org.openstreetmap.fma.jtiledownloader.config.DownloadConfigurationBBoxLatLon;
import org.openstreetmap.fma.jtiledownloader.config.DownloadConfigurationBBoxXY;
import org.openstreetmap.fma.jtiledownloader.config.DownloadConfigurationGPX;
import org.openstreetmap.fma.jtiledownloader.config.DownloadConfigurationUrlSquare;
import org.openstreetmap.fma.jtiledownloader.datatypes.DownloadJob;
import org.openstreetmap.fma.jtiledownloader.datatypes.Tile;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileDownloadError;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf;
import org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileList;

public class JTileDownloaderCommandLine
    implements TileDownloaderListener
{

    private static final Object CMDLINE_DL = "DL";

    private final HashMap<String, String> _arguments;

    private DownloadJob _downloadJob;
    private TileList _tileList;
    private TileListDownloader _tld;
    private TileProviderIf _tileProvider;

    /**
     * @param arguments
     */
    public JTileDownloaderCommandLine(HashMap<String, String> arguments)
    {
        _arguments = arguments;
    }

    /**
     * 
     */
    public void start()
    {
        printStartUpMessage();

        if (_arguments.containsKey(CMDLINE_DL))
        {
            String propertyFile = _arguments.get(CMDLINE_DL);

            _downloadJob = new DownloadJob(propertyFile);

            _tileProvider = _downloadJob.getTileProvider();

            handleDownloadTemplate(_downloadJob.getType());
        }
    }

    /**
     * @param type
     */
    private void handleDownloadTemplate(String type)
    {
        DownloadConfiguration _downloadTemplate = null;

        if (type.equalsIgnoreCase(DownloadConfigurationUrlSquare.ID))
        {
            _downloadTemplate = new DownloadConfigurationUrlSquare();
        }
        else if (type.equalsIgnoreCase(DownloadConfigurationBBoxLatLon.ID))
        {
            _downloadTemplate = new DownloadConfigurationBBoxLatLon();
        }
        else if (type.equalsIgnoreCase(DownloadConfigurationBBoxXY.ID))
        {
            _downloadTemplate = new DownloadConfigurationBBoxXY();
        }
        else if (type.equalsIgnoreCase(DownloadConfigurationGPX.ID))
        {
            _downloadTemplate = new DownloadConfigurationGPX();
        }
        else
        {
            log("File contains an unknown format. Please specify a valid file!");
        }

        if (_downloadTemplate != null)
        {
            _downloadJob.loadDownloadConfig(_downloadTemplate);

            _tileList = _downloadTemplate.getTileList(_downloadJob);

            startDownload(_tileProvider);
        }
    }

    /**
     * 
     */
    private void startDownload(TileProviderIf tileProvider)
    {
        _tld = new TileListDownloader(_downloadJob.getOutputLocation(), _tileList, tileProvider);
        _tld.setListener(this);
        if (_tileList.getTileListToDownload().size() > 0)
        {
            _tld.start();
        }
    }

    /**
     * 
     */
    private void printStartUpMessage()
    {
        log("JTileDownloader  Copyright (C) 2008  Friedrich Maier");
        log("This program comes with ABSOLUTELY NO WARRANTY.");
        log("This is free software, and you are welcome to redistribute it");
        log("under certain conditions");
        log("See file COPYING.txt and README.txt for details.");
        log("");
        log("");
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadComplete(int, java.util.Vector, int)
     */
    public void downloadComplete(int errorCount, Vector<TileDownloadError> errorTileList, int updatedTileCount)
    {
        log("updated " + updatedTileCount + " tiles");
        log("download completed with " + errorCount + " errors");
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadedTile(int, int, java.lang.String, boolean)
     */
    public void downloadedTile(int actCount, int maxCount, String path, int updatedCount, boolean updatedTile)
    {
        log("downloaded tile " + actCount + "/" + maxCount + " to " + path + ": updated flag is " + updatedTile);
    }

    /**
     * @param message 
     */
    public void waitResume(String message)
    {
        log("wait to resume: " + message);
    }

    /**
     * @param message 
     */
    public void waitWaitHttp500ErrorToResume(String message)
    {
        log("http 500 error occured: " + message);
    }

    /**
     * method to write to System.out
     * 
     * @param msg message to log
     */
    private static void log(String msg)
    {
        System.out.println(msg);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#errorOccured(int, int, Tile)
     */
    public void errorOccured(int actCount, int maxCount, Tile tile)
    {
        log("Error downloading tile " + actCount + "/" + maxCount + " from " + tile);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadStopped(int, int)
     */
    public void downloadStopped(int actCount, int maxCount)
    {
        log("Stopped download at  tile " + actCount + "/" + maxCount);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#setInfo(java.lang.String)
     */
    public void setInfo(String message)
    {
        log(message);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadPaused(int, int)
     */
    public void downloadPaused(int actCount, int maxCount)
    {}
}
