package test.lee.bindertest.aidl;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import java.util.List;

import test.lee.bindertest.Book;

/**
 * @author lihuayong
 * @version 1.0
 * @description IBookManager
 * @date 2020-01-16 15:13
 */
public interface IBookManager extends IInterface {
    /**
     * 文件全限定符
     */
    static final String DESCRIPTOR = "test.lee.bindertest.aidl.IBookManager";

    static final int TRANSACTION_getBookList = IBinder.FIRST_CALL_TRANSACTION + 0;
    static final int TRANSACTION_addBook = IBinder.FIRST_CALL_TRANSACTION + 1;
    static final int TRANSACTION_deleteBook = IBinder.FIRST_CALL_TRANSACTION + 2;
    static final int TRANSACTION_registerListener = IBinder.FIRST_CALL_TRANSACTION + 3;
    static final int TRANSACTION_unregisterListener = IBinder.FIRST_CALL_TRANSACTION + 4;

    public List<Book> getBookList() throws RemoteException;

    public void addBook(Book book) throws RemoteException;

    public boolean deleteBook(Book book) throws RemoteException;

    void registerListener(IOnNewBookArrivedListener listener) throws RemoteException;

    void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException;
}
