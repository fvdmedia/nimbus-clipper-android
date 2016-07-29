package com.fvd.utils;

public interface AsyncTaskCompleteListener<T,V>
{
    public void onTaskComplete(T result,V adv);
}
