package test.lee.bindertest.aidl;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import test.lee.bindertest.Book;

/**
 * @author lihuayong
 * @version 1.0
 * @description BookManagerService
 * @date 2020-01-16 15:55
 */

public class BookManagerService extends Service {
    private static final String TAG = "BookManagerService";

    ExecutorService executors;

    private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(false);
    /**
     * 存放书本的集合, CopyOnWriteArrayList支持多线程并发（内部加锁了）
     */
    private final CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();

    /**
     * 存放监听的集合，必须使用RemoteCallbackList，否则无法解注册，因为跨进程了。
     * Binder传递过来的对象在不同进程中是两个不同的对象，但是binder本身是只有一个，
     * 所以用binder本身做key来管理跨进城对象
     */
    private RemoteCallbackList<IOnNewBookArrivedListener> mListeners = new RemoteCallbackList<>();


    /**
     * 实现AIDL接口
     */
    private Binder binder = new AbstractBookManager() {

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            //权限验证 保证非验证请求不能通过
            int check = checkCallingOrSelfPermission("test.lee.bindertest.permission.ACCESS_BOOK_SERVICE");
            if (check == PackageManager.PERMISSION_DENIED) {
                Log.d("lhy", "book manger service:Permission denied!!!");
                return false;
            }

            String packageName = null;
            String[] packages = getPackageManager().getPackagesForUid(getCallingUid());
            if (packages != null && packages.length > 0) {
                packageName = packages[0];
            }
            //包名过滤
            if (packageName != null &&
                    (!packageName.startsWith("test.lee") || !packageName.startsWith("me.fresh.lee"))) {
                return false;
            }
            return super.onTransact(code, data, reply, flags);
        }

        @Override
        public List<Book> getBookList() throws RemoteException {
//            synchronized (mBookList) {
            return mBookList;
//            }
        }

        @Override
        public void addBook(Book book) throws RemoteException {
//            synchronized (mBookList) {
            Log.d("lhy", "add book " + book.toString());
            mBookList.add(book);
//            }
        }

        @Override
        public boolean deleteBook(Book book) throws RemoteException {
//            synchronized (mBookList) {
            Log.d("lhy", "delete book " + book.toString());
            return mBookList.remove(book);
//            }
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
            Log.d("lhy", "register IOnNewBookArrivedListener");
            Log.d("lhy-t", "service registerListener thread : " + Thread.currentThread().getName());
            mListeners.register(listener);
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
            Log.d("lhy", "unregister IOnNewBookArrivedListener");
            mListeners.unregister(listener);
        }
    };

    /**
     * 把新加的书本添加到集合中
     * 并且通知有新的书本增加
     *
     * @param book
     * @throws RemoteException
     */
    private void onNewBookArrived(Book book) throws RemoteException {
        Log.d("lhy-t", "service onNewBookArrived thread : " + Thread.currentThread().getName());
        mBookList.add(book);
        final int N = mListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            IOnNewBookArrivedListener listener = mListeners.getBroadcastItem(i);
            if (listener != null) {
                //通知
                Log.d("lhy", "on new book arrived");
                listener.onNewBookArrived(book);
            }
        }
        mListeners.finishBroadcast();
    }

    /**
     * 演示，每个一秒中增加一本书
     */
    private class ServiceWorker implements Runnable {

        @Override
        public void run() {
            while (!mIsServiceDestroyed.get()) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int bookId = mBookList.size() + 1;
                Book newBook = new Book(bookId, "new book#" + bookId);
                try {
                    //加入集合
                    onNewBookArrived(newBook);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("lhy", "book manager service onCreate");
        mBookList.add(new Book(1, "Android"));
        mBookList.add(new Book(2, "iOS"));
        executors = Executors.newFixedThreadPool(2);
        executors.execute(new ServiceWorker());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("lhy", "book manager service onBind");

        //权限验证
        int check = checkCallingOrSelfPermission("test.lee.bindertest.permission.ACCESS_BOOK_SERVICE");
        if (check == PackageManager.PERMISSION_DENIED) {
            Log.d("lhy", "book manger service:Permission denied!!!");
            return null;
        }
        return binder;
    }

    @Override
    public void onDestroy() {
        executors.shutdown();
        mIsServiceDestroyed.set(true);
        super.onDestroy();
    }
}
