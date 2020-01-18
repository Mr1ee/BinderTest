package test.lee.bindertest.aidl;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;

import test.lee.bindertest.Book;

/**
 * @author lihuayong
 * @version 1.0
 * @description AbstractBookManager 不借助AIDL文件，手动实现Binder对象
 * @date 2020-01-16 15:19
 */
public abstract class AbstractBookManager extends Binder implements IBookManager {

    public AbstractBookManager() {
        this.attachInterface(this, DESCRIPTOR);
    }

    public static IBookManager asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
        if (((iin instanceof test.lee.bindertest.IBookManager))) {
            return ((test.lee.bindertest.aidl.IBookManager) iin);
        }
        return new AbstractBookManager.Proxy(obj);
    }

    @Override
    public IBinder asBinder() {
        return this;
    }

    @Override
    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case INTERFACE_TRANSACTION: {
                reply.writeString(DESCRIPTOR);
                return true;
            }
            case TRANSACTION_getBookList: {
                Log.d("lhy", "Stub get book list");

                data.enforceInterface(DESCRIPTOR);
                List<Book> result = this.getBookList();
                reply.writeNoException();
                reply.writeTypedList(result);
                return true;
            }
            case TRANSACTION_addBook: {
                Log.d("lhy", "Stub add book ");

                data.enforceInterface(DESCRIPTOR);
                Book arg0;
                if ((0 != data.readInt())) {
                    arg0 = Book.CREATOR.createFromParcel(data);
                } else {
                    arg0 = null;
                }
                this.addBook(arg0);
                reply.writeNoException();
                return true;
            }
            case TRANSACTION_deleteBook: {
                Log.d("lhy", "Stub delete book ");

                data.enforceInterface(DESCRIPTOR);
                Book arg0;
                if ((0 != data.readInt())) {
                    arg0 = Book.CREATOR.createFromParcel(data);
                } else {
                    arg0 = null;
                }
                int result = this.deleteBook(arg0) ? 1 : 0;
                reply.writeNoException();
                reply.writeInt(result);
                return true;
            }
            case TRANSACTION_registerListener: {
                data.enforceInterface(DESCRIPTOR);
                IOnNewBookArrivedListener arg0;
                arg0 = IOnNewBookArrivedListener.Stub.asInterface(data.readStrongBinder());
                this.registerListener(arg0);
                reply.writeNoException();
                return true;
            }

            case TRANSACTION_unregisterListener: {
                data.enforceInterface(DESCRIPTOR);
                IOnNewBookArrivedListener arg0;
                arg0 = IOnNewBookArrivedListener.Stub.asInterface(data.readStrongBinder());
                this.unregisterListener(arg0);
                reply.writeNoException();
                return true;
            }
            default: {
                return super.onTransact(code, data, reply, flags);
            }
        }
    }

    private static class Proxy implements IBookManager {
        private android.os.IBinder mRemote;

        Proxy(android.os.IBinder remote) {
            mRemote = remote;
        }

        @Override
        public IBinder asBinder() {
            return mRemote;
        }

        public String getInterfaceDescriptor() {
            return DESCRIPTOR;
        }

        @Override
        public List<Book> getBookList() throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            Log.d("lhy", "Proxy get book list ");

            List<Book> result;
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                mRemote.transact(TRANSACTION_getBookList, data, reply, 0);
                reply.readException();
                result = reply.createTypedArrayList(Book.CREATOR);
            } finally {
                reply.recycle();
                data.recycle();
            }
            return result;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            Log.d("lhy", "Proxy add book ");
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                if ((book != null)) {
                    data.writeInt(1);
                    book.writeToParcel(data, 0);
                } else {
                    data.writeInt(0);
                }
                mRemote.transact(TRANSACTION_addBook, data, reply, 0);
                reply.readException();
            } finally {
                reply.recycle();
                data.recycle();
            }
        }

        @Override
        public boolean deleteBook(Book book) throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            Log.d("lhy", "Proxy delete book ");
            boolean result;
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                if ((book != null)) {
                    data.writeInt(1);
                    book.writeToParcel(data, 0);
                } else {
                    data.writeInt(0);
                }
                mRemote.transact(TRANSACTION_deleteBook, data, reply, 0);
                reply.readException();
                result = reply.readInt() == 1;
            } finally {
                reply.recycle();
                data.recycle();
            }

            return result;
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeStrongBinder((((listener != null)) ? (listener.asBinder()) : (null)));
                mRemote.transact(TRANSACTION_registerListener, data, reply, 0);
                reply.readException();
            } finally {
                reply.recycle();
                data.recycle();
            }
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeStrongBinder((((listener != null)) ? (listener.asBinder()) : (null)));
                mRemote.transact(TRANSACTION_unregisterListener, data, reply, 0);
                reply.readException();
            } finally {
                reply.recycle();
                data.recycle();
            }
        }
    }
}
