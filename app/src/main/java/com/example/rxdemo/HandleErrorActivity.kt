package com.example.rxdemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_handle_error.*
import java.io.IOException

class HandleErrorActivity : AppCompatActivity() {

    private var doOnError: Flowable<String>? = null
    private var doErrorComplete: Completable? = null
    private var doOnErrorResumeNext: Flowable<Int>? = null
    private var onErrorReturn: Flowable<Int>? = null
    private var retry: Flowable<Int>? = null
    private var backpressure: Flowable<Int>? = null
    private var checkScheduler2: Observable<Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_handle_error)

        initData()
        handleEvents()
    }

    private fun handleEvents() {

        btn_on_error_resume_next.setOnClickListener {
            doOnErrorResumeNext?.subscribe({
                makeLog("doOnErrorResumeNext: $it")
            }, {
                makeLog("doOnErrorResumeNext Err: ${it.message}")
            })
        }

        btn_do_on_error.setOnClickListener {
            doOnError?.subscribe({
                makeLog("doOnError: will never print $it")
            }, {
                makeLog("doOnError Err: $it")
            })
        }

        btn_do_err_complete.setOnClickListener {
            doErrorComplete?.subscribe({
                makeLog("doErrorComplete: completed  ")
            }, {
                makeLog(" Err: $it")
            })
        }

        btn_on_error_return.setOnClickListener {
            onErrorReturn?.subscribe({
                makeLog(" onErrorReturn: $it")
            }, {
                makeLog(" Err: $it")
            })
        }

        btn_retry.setOnClickListener {
            retry?.subscribe({
                makeLog(" retry: $it")
            }, {
                makeLog(" Err: $it")
            })
        }

        btn_scheduler.setOnClickListener {
            backpressure?.subscribe({
                makeLog("onNext: $it")
            }, {
                makeLog("Err: ${it.message}")
            })
        }

        btn_scheduler2.setOnClickListener {
            checkScheduler2?.subscribe({
                makeLog("onNext: $it")
            }, {
                makeLog("Err: ${it.message}")
            })
        }
    }

    private fun initData() {

        backpressure = Flowable.create<Int>({ emitter ->
            for (i in 0 until 7) {
                makeLog("emitting: $i")
                emitter.onNext(i)
            }
        }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread(), false, 3)

        checkScheduler2 = Observable.create<Int> { emitter ->
            for (i in 0 until 7) {
                makeLog("emitting: $i")
                emitter.onNext(i)
            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread(), false, 3)

        retry = Flowable.create<Int>({ emitter ->
            emitter.onNext(1)
            emitter.onNext(2)
            emitter.onNext(3 / 0) //ArithmeticException: divide by zero
        }, BackpressureStrategy.BUFFER)
            .retry { retryCount, error ->
                retryCount < 3 && error !is IOException
            }

        onErrorReturn = Flowable.just("3A").map { v ->
            v.toInt() // NumberFormatException: For input string: "3A"
        }.onErrorReturn { error ->
            if (error is NumberFormatException)
                3
            else throw IllegalArgumentException("sth went wrong!")
        }

        doOnErrorResumeNext = Flowable.create<Int>({ emitter ->
            emitter.onNext(1)
            emitter.onNext(2)
            emitter.onNext(3 / 0) //ArithmeticException: divide by zero
            emitter.onNext(4)
            emitter.onNext(5)
        }, BackpressureStrategy.BUFFER)
            .onErrorResumeNext(Flowable.just(3, 4, 5))

        doErrorComplete = Completable.fromAction {
            3 / 0 // ArithmeticException: divide by zero
        }.onErrorComplete { ex ->
            return@onErrorComplete ex is ArithmeticException
        }

        doOnError = Flowable.error<String>(Throwable(" Something went wrong!!"))
            .doOnError {
                // do something here !
                makeLog("doOnError: ${it.message}")
            }
    }

    private fun doSomeThings2(num: Int): Int {
        Thread.sleep(2000)
        return num
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, HandleErrorActivity::class.java)
    }
}
