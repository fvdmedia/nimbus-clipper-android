package com.fvd.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class WebHistoryKeeper
{
    private static WebHistoryKeeper instance;

    public static synchronized WebHistoryKeeper getInstance()
    {
        if (instance == null)
        {
            instance = new WebHistoryKeeper();
        }
        return instance;
    }

    public WebHistoryKeeper()
    {
        m_HistoryItemsSet = new ArrayList<HistoryItem>();
        m_hostNamesMap = new TreeMap<String, HistoryItem>();
        LoadHistory();
    }
    
    public String getHost(String url)
    {
        String host = null;
        if (url != null)
        {
            Uri uri = Uri.parse(url);

            host = uri.getHost();
            if (host != null)
            {
                String[] items = host.split("\\.");
                if(items.length>2)
                {
                    host = items[items.length-2] + '.' + items[items.length-1];
                }
            }
        }
        else
        {
            
        }

        return host;
    }

    public void AddHistoryItem(String urlLink, String desc, HistoryItem.ETipType itemType)
    {
        String url = urlLink.toLowerCase();

        String host =getHost(url);
        if (host != null)
        {
            if (host != null)
            {
                HistoryItem hi = findItem(host);
                if (hi == null)
                {
                    AddNewHost(host, desc, itemType);
                }
                else
                {
                    IncreaseVisitedCounterFor(hi);
                }

                SaveHistory();
            }
        }
    }

    public void UpdateItemDescription(String urlLink, String desc)
    {
        String url = urlLink.toLowerCase();
        String host = getHost(url);

        if (host != null)
        {
            HistoryItem hi = findItem(host);
            if (hi != null)
            {
                hi.desc = desc;
            }
            else
            {
                
            }
        }
    }

    public void ClearHistory()
    {
        DeleteHistoryFile();
        DeleteHistoryItems();
    }

    protected void DeleteHistoryFile()
    {
        String filename = getFileName();
        File f = new File(filename);
        f.delete();
    }

    protected void DeleteHistoryItems()
    {
        m_HistoryItemsSet.clear();
        m_hostNamesMap.clear();
    }

    protected void AddNewHost(String host, String desc, HistoryItem.ETipType itemType)
    {
        HistoryItem historyItem = new HistoryItem(host, host, desc, itemType);
        int index = Collections.binarySearch(m_HistoryItemsSet, historyItem, new HistoryItemComparator() );
        if (index < 0)
        {
            index = -index - 1;
        }
        m_HistoryItemsSet.add(index, historyItem);

        m_hostNamesMap.put(host, historyItem);
    }

    protected void IncreaseVisitedCounterFor(HistoryItem hi)
    {
        hi.requests++;

        Collections.sort(m_HistoryItemsSet, new HistoryItemComparator());
    }

    public ArrayList<HistoryItem> getItemsFor(String firstLetters)
    {
        ArrayList<HistoryItem> res = new  ArrayList<HistoryItem>();
        int itemsToGet = getTreshold(FIRST_ITEMS_COUNT_TO_GET);
        for (HistoryItem historyItem: m_HistoryItemsSet)
        {
            if (historyItem.url.startsWith(firstLetters))
            {
                res.add(historyItem);
                if (res.size() >= itemsToGet)
                {
                    break;
                }
            }
        }

        return res;
    }

    protected void LoadHistory()
    {
        String filename = getFileName();
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            while (br.ready())
            {
                HistoryItem historyItem = new HistoryItem();
                if (historyItem.Load(br))
                {
                    m_HistoryItemsSet.add(historyItem);
                    m_hostNamesMap.put(historyItem.url, historyItem);
                }
            }

            br.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    protected void SaveHistory()
    {
        if (++m_nSaveTransactionsCount % 2 != 0)
        {
            String filename = getFileName();
            BufferedWriter bw = null;
            try
            {
                int nItemsToSave = getTreshold(MAX_ITEMS_TO_SAVE );
                int nItemsSaved = 0;
                bw = new BufferedWriter(new FileWriter(filename));
                for (HistoryItem historyItem: m_HistoryItemsSet)
                {
                    nItemsSaved++;
                    if (nItemsSaved > nItemsToSave)
                    {
                        break;
                    }
                    else
                    {
                        historyItem.SaveTo(bw);
                        bw.write("\n");
                    }
                }
                bw.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    protected int getTreshold(int treshold)
    {
        int size = m_HistoryItemsSet.size();
        if (size > treshold)
        {
            size = treshold;
        }

        return size;
    }

    protected  HistoryItem findItem(String host)
    {
        HistoryItem res = m_hostNamesMap.get(host);
        return res;
    }
    
    public String getAppName()
    {
        String app = "Nimbus Clipper";
        return app;
    }
    
    public String getAppFolder()
    {
        File folder = new File(Environment.getExternalStorageDirectory().toString()+"/" + getAppName());
        folder.mkdirs();

        return folder.toString();
    }
    
    protected String getFileName()
    {
        String str = getAppFolder() + "/" + HISTORY_FILE_NAME;

        return str;
    }

    class HistoryItemComparator implements Comparator<HistoryItem>
    {
        @Override
        public int compare(HistoryItem historyItem, HistoryItem historyItem2)
        {
            int diff = -(historyItem.requests - historyItem2.requests);
            return diff;
        }
    }

    static protected final String HISTORY_FILE_NAME = "History.txt";
    static protected final int MAX_ITEMS_TO_SAVE = 100;
    static protected final int FIRST_ITEMS_COUNT_TO_GET = 8;

    int m_nSaveTransactionsCount = 0;

    ArrayList<HistoryItem> m_HistoryItemsSet;
    TreeMap<String, HistoryItem> m_hostNamesMap;


}