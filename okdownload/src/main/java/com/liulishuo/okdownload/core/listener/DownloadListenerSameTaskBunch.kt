/*
 * Copyright (c) 2018 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liulishuo.okdownload.core.listener

import android.os.Handler
import com.liulishuo.okdownload.DownloadListener
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause

/**
 * Author: raynor
 * Date: 2020/12/22 2:55 PM
 * Description:
 */
class DownloadListenerSameTaskBunch(val uiHandler: Handler, val listeners: Array<DownloadListenerWrapper>) : DownloadListener {
    /**
     * 包装[DownloadListener] 的代理类，没有任何额外逻辑，只是用来保存 实际[DownloadListener] 回调的线程
     */
    class DownloadListenerWrapper(val autoCallbackToUIThread: Boolean, listener: DownloadListener) : DownloadListener by listener

    override fun taskStart(task: DownloadTask) {
        for (listener in listeners) {
            listener.taskStart(task)
        }
    }

    override fun connectTrialStart(
        task: DownloadTask,
        requestHeaderFields: Map<String?, List<String?>?>
    ) {
        for (listener in listeners) {
            if (listener.autoCallbackToUIThread) {
                uiHandler.post {
                    listener.connectTrialStart(task, requestHeaderFields)
                }
            } else {
                listener.connectTrialStart(task, requestHeaderFields)
            }
        }
    }

    override fun connectTrialEnd(
        task: DownloadTask, responseCode: Int,
        responseHeaderFields: Map<String?, List<String?>?>
    ) {
        for (listener in listeners) {
            if (listener.autoCallbackToUIThread) {
                uiHandler.post {
                    listener.connectTrialEnd(task, responseCode, responseHeaderFields)
                }
            } else {
                listener.connectTrialEnd(task, responseCode, responseHeaderFields)

            }
        }
    }

    override fun downloadFromBeginning(
        task: DownloadTask, info: BreakpointInfo,
        cause: ResumeFailedCause
    ) {
        for (listener in listeners) {
            if (listener.autoCallbackToUIThread) {
                uiHandler.post {
                    listener.downloadFromBeginning(task, info, cause)
                }
            } else {
                listener.downloadFromBeginning(task, info, cause)
            }
        }
    }

    override fun downloadFromBreakpoint(task: DownloadTask, info: BreakpointInfo) {
        for (listener in listeners) {
            if (listener.autoCallbackToUIThread) {
                uiHandler.post {
                    listener.downloadFromBreakpoint(task, info)
                }
            } else {
                listener.downloadFromBreakpoint(task, info)
            }
        }


    }

    override fun connectStart(
        task: DownloadTask, blockIndex: Int,
        requestHeaderFields: Map<String?, List<String?>?>
    ) {
        for (listener in listeners) {
            if (listener.autoCallbackToUIThread) {
                uiHandler.post {
                    listener.connectStart(task, blockIndex, requestHeaderFields)
                }
            } else {
                listener.connectStart(task, blockIndex, requestHeaderFields)
            }
        }
    }

    override fun connectEnd(
        task: DownloadTask, blockIndex: Int, responseCode: Int,
        responseHeaderFields: Map<String?, List<String?>?>
    ) {
        for (listener in listeners) {
            if (listener.autoCallbackToUIThread) {
                uiHandler.post {
                    listener.connectEnd(task, blockIndex, responseCode, responseHeaderFields)
                }
            } else {
                listener.connectEnd(task, blockIndex, responseCode, responseHeaderFields)
            }
        }
    }

    override fun fetchStart(task: DownloadTask, blockIndex: Int, contentLength: Long) {
        for (listener in listeners) {
            if (listener.autoCallbackToUIThread) {
                uiHandler.post {
                    listener.fetchStart(task, blockIndex, contentLength)
                }
            } else {
                listener.fetchStart(task, blockIndex, contentLength)
            }
        }
    }

    override fun fetchProgress(task: DownloadTask, blockIndex: Int, increaseBytes: Long) {
        for (listener in listeners) {
            if (listener.autoCallbackToUIThread) {
                uiHandler.post {
                    listener.fetchProgress(task, blockIndex, increaseBytes)
                }
            } else {
                listener.fetchProgress(task, blockIndex, increaseBytes)
            }
        }
    }

    override fun fetchEnd(task: DownloadTask, blockIndex: Int, contentLength: Long) {
        for (listener in listeners) {
            if (listener.autoCallbackToUIThread) {
                uiHandler.post {
                    listener.fetchEnd(task, blockIndex, contentLength)
                }
            } else {
                listener.fetchEnd(task, blockIndex, contentLength)

            }
        }
    }

    override fun taskEnd(
        task: DownloadTask, cause: EndCause,
        realCause: Exception?
    ) {
        for (listener in listeners) {
            if (listener.autoCallbackToUIThread) {
                uiHandler.post {
                    listener.taskEnd(task, cause, realCause)
                }
            } else {
                listener.taskEnd(task, cause, realCause)
            }
        }
    }

    fun contain(targetListener: DownloadListener): Boolean {
        for (listener in listeners) {
            if (listener === targetListener) return true
        }
        return false
    }

    /**
     * Get the index of `targetListener`, smaller index, earlier to receive callback.
     *
     * @param targetListener used for compare and get it's index on the bunch.
     * @return `-1` if can't find `targetListener` on the bunch, otherwise the index of
     * the `targetListener` on the bunch.
     */
    fun indexOf(targetListener: DownloadListener): Int {
        for (index in listeners.indices) {
            val listener = listeners[index]
            if (listener === targetListener) return index
        }
        return -1
    }

    fun toBuilder(): Builder {
        return Builder(listeners.toMutableList())

    }

    class Builder(private val listeners: MutableList<DownloadListenerWrapper> = ArrayList()) {

        fun build(uiHandler: Handler): DownloadListenerSameTaskBunch {
            return DownloadListenerSameTaskBunch(
                uiHandler,
                listeners.toTypedArray()
            )
        }

        /**
         * Append `listener` to the end of bunch listener list. Then the `listener` will
         * listener the callbacks of the host bunch listener attached.
         *
         * @param listener will be appended to the end of bunch listener list. if it's `null`,
         * it will not be appended.
         */
        fun append(listener: DownloadListenerWrapper): Builder {
            if (!listeners.contains(listener)) {
                listeners.add(listener)
            }
            return this
        }

        fun remove(listener: DownloadListenerWrapper): Boolean {
            return listeners.remove(listener)
        }
    }
}