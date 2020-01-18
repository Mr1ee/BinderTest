// IOnNewBookArrivedListener.aidl
package test.lee.bindertest;

// Declare any non-default types here with import statements
import test.lee.bindertest.Book;

interface IOnNewBookArrivedListener2 {
    void onNewBookArrived(in Book newBook);
}
