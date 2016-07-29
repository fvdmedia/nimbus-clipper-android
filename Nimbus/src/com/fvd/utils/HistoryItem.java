package com.fvd.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class HistoryItem
{
    public enum ETipType
    {
        TIP_HOST,
        TIP_KEY,
        TIP_SEARCH
    }

    public HistoryItem()
    {
    }

    public HistoryItem(String url, String visibleUrl, String desc, ETipType type)
    {
        this.url = url;
        this.desc = desc;
        this.type = type;
        this.visibleUrl = visibleUrl;
        this.requests = 1;

        if (this.visibleUrl == null)
        {
            this.visibleUrl = url;
        }

        if (this.desc == null || this.desc.length() == 0)
        {
            this.desc = this.visibleUrl;
        }
    }


    public void SaveTo(BufferedWriter br)
    {
        if (desc != null && url != null && type == ETipType.TIP_HOST)
        {
            String str = url + DELIMETER + String.valueOf(requests) + DELIMETER + visibleUrl + DELIMETER + desc;
            try
            {
                br.write(str);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public boolean Load(BufferedReader reader)
    {
        boolean res = false;

        try
        {
            String line = reader.readLine();
            String[] parts = line.split(DELIMETER);
            if (parts.length == 4)
            {
                url         = parts[0];
                requests    = Integer.valueOf(parts[1]);
                visibleUrl  = parts[2];
                desc        = parts[3];
                type        = ETipType.TIP_HOST;
                res = true;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();  
        }

        return res;
    }

    public String url;
    public String desc;
    public String visibleUrl;
    public ETipType type;
    public int requests = 0;

    protected static final String DELIMETER = ";";
}