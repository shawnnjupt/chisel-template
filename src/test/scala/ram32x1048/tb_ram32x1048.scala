//********************************************************************************
// Project Name : test_chisel
// Author       : CX
// Create Date  : 2025.01
// Modify Date  :
// Version      : v0.1
//--------------------------------------------------------------------------------
// Description :
//      ram32x1024 in chisel
// Additional Comments :
//
//********************************************************************************
package ram

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers


class Ram32x1024SyncTest  extends AnyFreeSpec with Matchers {
  "Ram32x1024Sync should work correctly" in {
    simulate(new ram32x1024_sync) {dut =>
            // 初始化信号
            dut.io.wr.poke(false.B)
            dut.io.rd.poke(false.B)
            dut.io.addr.poke(0.U)
            dut.io.wdata.poke(0.U)
            dut.io.byte_en.foreach(_.poke(false.B))  // 初始不使能任何字节

            // 测试1: 基本写后读
            val testAddr = 42.U
            val testData = 0x12345678L.U(32.W)

            // 写入操作
            dut.io.wr.poke(true.B)
            dut.io.addr.poke(testAddr)
            dut.io.wdata.poke(testData)
            dut.io.byte_en.foreach(_.poke(true.B))  // 使能全部4字节
            dut.clock.step(1)

            // 读取操作
            dut.io.wr.poke(false.B)
            dut.io.rd.poke(true.B)
            dut.clock.step(1)
            dut.io.rdata.expect(testData)  // 同步RAM应在下一周期返回数据
            println(s"Test 1: Read data = ${dut.io.rdata.peek().litValue.toInt.toHexString}")

            // 测试2: 字节使能测试
            val partialData = 0xAABBCCDDL.U(32.W)

            // 只写入低16位（字节0和1）
            dut.io.wr.poke(true.B)
            dut.io.addr.poke(testAddr)
            dut.io.wdata.poke(partialData)
            dut.io.byte_en(0).poke(true.B)
            dut.io.byte_en(1).poke(true.B)
            dut.io.byte_en(2).poke(false.B)
            dut.io.byte_en(3).poke(false.B)
            dut.clock.step(1)

            // 读取验证
            dut.io.wr.poke(false.B)
            dut.io.rd.poke(true.B)
            dut.clock.step(1)
            println(s"Test 2: Read data = ${dut.io.rdata.peek().litValue.toInt.toHexString}")
            // 预期结果：高16位保持原值（0x1234），低16位更新为0xBBAA
            dut.io.rdata.expect(0x1234CCDDL.U)



            // 测试3: 地址边界测试
            val maxAddr = 1023.U
            val edgeData = 0xCAFEBABEL.U

            // 写入最大地址
            dut.io.wr.poke(true.B)
            dut.io.addr.poke(maxAddr)
            dut.io.wdata.poke(edgeData)
            dut.io.byte_en.foreach(_.poke(true.B))
            dut.clock.step(1)

            // 读取验证
            dut.io.wr.poke(false.B)
            dut.io.rd.poke(true.B)
            dut.clock.step(1)
            println(s"Test 3: Read data = ${dut.io.rdata.peek().litValue.toInt.toHexString}")
            dut.io.rdata.expect(edgeData)


    }
  }
}
