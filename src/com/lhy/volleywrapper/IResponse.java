package com.lhy.volleywrapper;

/**
 * 
 * @author liuhy0206@gmail.com
 * 
 * @param <T>
 */
public interface IResponse<T> {
    public void onRespose(T response);

    public void onError(String error);
}
