# Chisel temple (edit by shawn)

## 1、chisel flow

### design

chisel代码如下

```scala
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
}

```

### chiselSim 

目前的chiselSim暂时只能小仿真，调用verilator的 api

```scala

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

    }
  }
}

```

### sbt command

```scala
sbt run
sbt test
```

## 2、EDA(VCS) SIM flow

环境: ubuntu20.04  wsl2

vcs破解参照:[v c s 2016&v e r d i2016安装及调试总结(教程) - IC验证讨论 - EETOP 创芯网论坛 (原名：电子顶级开发网) -](https://bbs.eetop.cn/thread-893428-1-1.html)

### VCS setup

```python
#command
act_vcs
err_vcs
#如果遇到hostid不对，需要更新 ifconfig里面的 ether:dc值生成新liscence
#!!!!!!!in bashrc
alias act_vcs='lmgrd -c /synopsys/vcs2016.06/license/Synopsys.dat' #注意改路径
alias err_vcs='lmdown'
```

### 编写tb

```systemverilog
`timescale 1ns/1ps
module tb(
);

logic clk_i;
logic rst_n;

initial begin
    clk_i = 1;
    rst_n = 0;

    # 95;

 #10000 $finish;
end

always #5 clk_i = ~clk_i;

initial begin
    $fsdbDumpfile("ram.fsdb");
    $fsdbDumpvars;
end

endmodule
```

### 丢到VCS里面SIM

make all 编译

make verdi 看波形

脚本如下:

```makefile
.PHONY:com sim clean

tab=/synopsys/verdi2016.06/share/PLI/VCS/LINUX64/novas.tab 
pli=/synopsys/verdi2016.06/share/PLI/VCS/LINUX64/pli.a 
top=tb

all: clean comp run

clean:
	rm -rf simv* csrc *.log vc_hdrs.h ucli.key ./proj ./verdiLog *.fsdb ./DVEfiles

comp:
	vcs -full64 -cpp g++-4.8 -cc gcc-4.8 -LDFLAGS -Wl,--no-as-needed  +v2k -fsdb +define+FSDB -sverilog  -kdb -lca -debug_pp -f ./verilog_file.f  -timescale=1ns/1ns -top tb -P ${tab} ${pli}  -l comp.log 

run:
	./simv  +UVM_NO_RELNOTES  -l run.log

verdi:
	verdi \
	-f ./verilog_file.f \                     #用Verdi加载verif.f所列全部源文件
	-top ${top}  \
	-ssf *.fsdb &                    #启动Verdi查看fsdb类型的波形文件

```

### VCS快捷键

g   get, signlas添加信号，显示波形

n   next, Search Forward选定信号按指定的值（上升沿，下降沿，both,指定Value）向前跳转

N   与n功能相同，方向向后

y   Keep Cursor at Centor（开关）移至中央并保持居中，再按取消固定居中

c   color,调整所选信号的波形显示颜色，线的粗细和类型，非常方便Debug

f   full, Zoom All波形全部显示

z   Zoom Out波形缩小，一般配合鼠标放大非常方便

Z   Zoom In 波形放大

l   last view，上次波形位置，相当于Vim里的``或''

L   重新加载波形或设计文件，这个很方便，在新一次仿真完成之后Roload即可

b   begin移动Cursor到波形开头

e   end移动Cursor到波形结尾

r   restore signals 保存波形信号列表

h   hierarchy显示信号的绝对路径

H   Highlight（开关）是否高亮显示所选信号

m   move将信号移动到黄线位置

Delete   删除所选信号

Ctrl+Right Arrow   向右移动半屏

Ctrl+Left Arrow   向左移动半屏

鼠标：

左键：用于选择信号

右键：调出菜单

中间：单击移动黄线，拖动信号可移动位置排列顺序

滑轮：上下滚屏

左键圈定波形范围：按选定缩放

双击信号波形： 跳转到代码，并用绿色高亮该信号

双击信号：按位展开(expand)，Struct展开下一层。再双击折叠(collapse)

右键信号名->Bus Operations->Expand as Sub-bus->可以按指定位宽展开，比如512bits的信号分成4个128的，方便查看

