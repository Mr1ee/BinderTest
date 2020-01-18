package test.lee.bindertest

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_book_service.*
import test.lee.bindertest.aidl.AbstractBookManager
import test.lee.bindertest.aidl.BookManagerService
import test.lee.bindertest.aidl.IBookManager
import test.lee.bindertest.aidl.IOnNewBookArrivedListener
import test.lee.bindertest.messenger.MessengerActivity
import kotlin.random.Random


class BookServiceActivity : AppCompatActivity() {

    private val MESSAGE_NEW_BOOK_ARRIVED: Int = 0x01;
    private var mRemoterBookManager: IBookManager? = null

    private lateinit var books: List<Book>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_service)
        val service = Intent(this, BookManagerService::class.java)
        bindService(service, connection, Context.BIND_AUTO_CREATE)

        parseOutIntent(intent)

        btn_add.setOnClickListener {
            mRemoterBookManager?.addBook(randomBook())
        }

        btn_sub.setOnClickListener {
            mRemoterBookManager?.deleteBook(randomDeletedBook())
            books = mRemoterBookManager?.bookList ?: arrayListOf()
        }

        // Intent.FLAG_ACTIVITY_NO_HISTORY stack中不会保留该activity，
        //e.g. A->B->C , B设置了Intent.FLAG_ACTIVITY_NO_HISTORY，则从C按返回键时会直接返回到A，而不会返回B
        btn_msg.setOnClickListener {
            startActivity(Intent(this, MessengerActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            })
        }

        btn_book_list.setOnClickListener {
            val str = mRemoterBookManager?.bookList?.fold(StringBuilder()) { acc, book ->
                acc.append(book.toString()).append("\n")
            }
            Log.d("lhy", "book list = $str")
            tv_show.text = str
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        parseOutIntent(getIntent())
    }

    /**
     *  adb shell am start test.lee.bindertest/.BookServiceActivity
     * 解析外部链接
     * adb shell am start -W -a android.intent.action.VIEW -d "lee://test.lee.binder/s2?key=123&id=456" test.lee.bindertest
     */
    private fun parseOutIntent(intent: Intent) {
        val action = intent.action
        val uri = intent.data

        val id = uri?.getQueryParameter("id")
        val key = uri?.getQueryParameter("key")
        Log.d("lhy", "key = $key, id = $id")

        when (uri?.lastPathSegment) {
            "s2" -> startActivity(Intent(this, SecondActivity::class.java))
            else -> return
        }
    }

    private fun randomDeletedBook(): Book {
        return books.get(Random(System.currentTimeMillis()).nextInt(books.size))
    }

    private fun randomBook(): Book {
        return Book(
            Random(System.currentTimeMillis()).nextInt(1000),
            "随机测试+" + Random(System.currentTimeMillis()).nextInt(10)
        )
    }

    override fun onDestroy() {
        if (mRemoterBookManager != null && mRemoterBookManager?.asBinder()?.isBinderAlive == true) {
            try {
                mRemoterBookManager?.unregisterListener(mListener)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

        }

        unbindService(connection)
        super.onDestroy()
    }

    /**
     * onServiceDisconnected和onServiceConnected都运行在客户端的UI线程，所以不能做耗时操作
     * 包括访问的服务端的接口也不能做耗时操作，因为这里会挂起等待服务端返回数据
     * 如果确定服务端的接口比较耗时，建议使用子线程与服务端通信
     */
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("lhy", "book manager service disconnect!")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("lhy-t", "onServiceConnected thread : ${Thread.currentThread().name}")
            Log.d("lhy", "book manager service connect success!")
            val bookManager: IBookManager = AbstractBookManager.asInterface(service)
            try {
                mRemoterBookManager = bookManager
                mRemoterBookManager?.asBinder()?.linkToDeath(mDeathRecipient, 0)
                val newBook = Book(3, "Android 艺术开发探索")
                bookManager.addBook(newBook)
                val list = bookManager.bookList
                books = list
                val str = list.fold(StringBuilder()) { acc, book ->
                    acc.append(book.toString()).append("\n")
                }
                Log.d("lhy", "book list = $str")
                bookManager.registerListener(mListener)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * binder服务端关闭之后，service重连
     */
    private val mDeathRecipient = object : IBinder.DeathRecipient {
        override fun binderDied() {
            mRemoterBookManager?.let {
                it.asBinder()?.unlinkToDeath(this, 0)
                mRemoterBookManager = null
                val service = Intent(this@BookServiceActivity, BookManagerService::class.java)
                bindService(service, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            Log.d("lhy-t", "Handler thread : ${Thread.currentThread().name}")
            when (msg.what) {
                MESSAGE_NEW_BOOK_ARRIVED -> Log.i("lhy", "receive new book:" + msg.obj)
                else -> super.handleMessage(msg)
            }
        }
    }

    /**
     * Warning！！！
     * 这里IOnNewBookArrivedListener运行在客户端的Binder线程中，所以不能去访问UI相关内容
     * 如果要修改UI，可以利用Handler
     */
    private val mListener = object : IOnNewBookArrivedListener.Stub() {
        @Throws(RemoteException::class)
        override fun onNewBookArrived(newBook: Book) {
            Log.d("lhy-t", "onNewBookArrived thread : ${Thread.currentThread().name}")
            mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED, newBook).sendToTarget()
        }
    }
}
