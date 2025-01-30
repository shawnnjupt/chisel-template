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
import chisel3.util._
import _root_.circt.stage.ChiselStage


class ram32x1024_sync extends Module {
  val io =IO(new Bundle {
    val wr=Input(Bool())
    val wdata=Input(UInt(32.W))
    val rd=Input(Bool())
    val addr=Input(UInt(10.W))
    val byte_en=Input(Vec(4,Bool()))
    val rdata=Output(UInt(32.W))
  })
  val wdata_temp=Wire(Vec(4,UInt(8.W)))
  val rdata_temp=Wire(Vec(4,UInt(8.W)))
  rdata_temp := DontCare
  wdata_temp := DontCare

  val syncMem = SyncReadMem(1024, Vec(4, UInt(8.W)))

  io.rdata:=0.U

  when(io.wr){
    syncMem.write(io.addr,wdata_temp,io.byte_en)

  }.otherwise{
    when(io.rd){
      rdata_temp:=syncMem.read(io.addr)
    }
    for (i <- 0 until 4) {
      wdata_temp(i) := io.wdata(8 * i + 7, 8 * i)
      io.rdata := Cat(rdata_temp(3), rdata_temp(2), rdata_temp(1),  rdata_temp(0))
    }
  }
}


object ram32x1024_sync extends App {
  ChiselStage.emitSystemVerilogFile(
    new ram32x1024_sync,
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
  )
  //   (new ChiselStage).emitVerilog(new Hello())
}
