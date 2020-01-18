package test.lee.bindertest.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import test.lee.bindertest.Book;

/**
 * @author lihuayong
 * @version 1.0
 * @description IOnNewBookArrivedListener
 * @date 2020-01-16 19:01
 */
public interface IOnNewBookArrivedListener extends IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends Binder implements IOnNewBookArrivedListener {
        private static final String DESCRIPTOR = "test.lee.bindertest.IOnNewBookArrivedListener";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an test.lee.bindertest.IOnNewBookArrivedListener interface,
         * generating a proxy if needed.
         */
        public static IOnNewBookArrivedListener asInterface(android.os.IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin instanceof IOnNewBookArrivedListener) {
                return ((IOnNewBookArrivedListener) iin);
            }
            return new Stub.Proxy(obj);
        }

        @Override
        public android.os.IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            java.lang.String descriptor = DESCRIPTOR;
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(descriptor);
                    return true;
                }
                case TRANSACTION_onNewBookArrived: {
                    Log.d("lhy", "Stub onTransact onNewBookArrived!");
                    data.enforceInterface(descriptor);
                    Book arg0;
                    if ((0 != data.readInt())) {
                        arg0 = Book.CREATOR.createFromParcel(data);
                    } else {
                        arg0 = null;
                    }
                    this.onNewBookArrived(arg0);
                    reply.writeNoException();
                    return true;
                }
                default: {
                    return super.onTransact(code, data, reply, flags);
                }
            }
        }

        private static class Proxy implements IOnNewBookArrivedListener {
            private IBinder mRemote;

            Proxy(IBinder remote) {
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
            public void onNewBookArrived(Book newBook) throws RemoteException {
                Log.d("lhy", "Proxy onNewBookArrived!");

                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    if ((newBook != null)) {
                        data.writeInt(1);
                        newBook.writeToParcel(data, 0);
                    } else {
                        data.writeInt(0);
                    }
                    mRemote.transact(TRANSACTION_onNewBookArrived, data, reply, 0);
                    reply.readException();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }
        }

        static final int TRANSACTION_onNewBookArrived = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    }

    public void onNewBookArrived(Book newBook) throws RemoteException;
}
