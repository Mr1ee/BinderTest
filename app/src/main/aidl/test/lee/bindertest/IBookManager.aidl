// IBookManager.aidl
package test.lee.bindertest;

// Declare any non-default types here with import statements
import test.lee.bindertest.Book;
import test.lee.bindertest.IOnNewBookArrivedListener2;

interface IBookManager {
    List<Book>getBookList();
    void addBook(in Book book);
    void registerListener(IOnNewBookArrivedListener2 listener);
    void unregisterListener(IOnNewBookArrivedListener2 listener);
}
