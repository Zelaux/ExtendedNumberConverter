package com.zelaux.numberconverter.utils

import arc.struct.ByteSeq
import java.io.PrintStream

class IndentStream(printStream: PrintStream) : PrintStream(printStream) {
    private var indentAmount = 0;
    private fun increaseIndent() {
        indentAmount++;
        while (indentAmount >= byteSeq.size && byteSeq.size < byteSeq.items.size) {
            byteSeq.addAll(*indentBytes)
        }
    }

    private fun decreaseIndent() {
        if (indentAmount == 0) throw IllegalArgumentException("Nothing to decrease");
        indentAmount--

    }

    fun indent(block: () -> Unit) {
        increaseIndent()
        try {
            block()
        } finally {
            decreaseIndent()
        }
    }

    private var wasNextLine = false;
    override fun write(buf: ByteArray, off: Int, len: Int) {
        var prevIndex = 0;
        if (wasNextLine) {
            super.write(byteSeq.items, 0, indentAmount)
            wasNextLine = false;
        }
        for (i in off until len) {
            var placeIndent = false
            if (buf[i] == systemSeparatorBytes.last() && i - systemSeparatorBytes.lastIndex>=0) {
                placeIndent = true
                for (j in systemSeparatorBytes.indices) {
                    if (buf[i - systemSeparatorBytes.lastIndex + j] != systemSeparatorBytes[j]) {
                        placeIndent = false
                        break
                    }
                }

            }
            if (buf[i] == nextLineByte && i - nextLineIndex>=0) {
                for (j in systemSeparatorBytes.indices) {
                    if (buf[i - nextLineIndex + j] != systemSeparatorBytes[j]) {
                        placeIndent = true
                        break
                    }
                }
            }
            if (placeIndent) {
                super.write(buf, off + prevIndex, i - prevIndex + 1)
                if (i < len - 1) {
                    super.write(byteSeq.items, 0, indentAmount * indentBytes.size)
                } else {
                    wasNextLine = true
                }

                prevIndex = i + 1
            }
        }
        super.write(buf, off + prevIndex, len - prevIndex)

    }

    companion object {
        val systemSeparatorBytes = System.lineSeparator().toByteArray()
        val indentBytes = byteArrayOf('\t'.code.toByte())
        const val nextLineByte = '\n'.code.toByte()
        val nextLineIndex = systemSeparatorBytes.indexOf(nextLineByte)
        val byteSeq = ByteSeq(indentBytes.size * 8).apply {
            for (i in 0 until 8) {
                addAll(*indentBytes)
            }
        }
    }

}