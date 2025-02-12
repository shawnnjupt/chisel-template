// Generated by CIRCT firtool-1.62.1
// VCS coverage exclude_file
module syncMem_1024x32(
  input  [9:0]  R0_addr,
  input         R0_en,
                R0_clk,
  output [31:0] R0_data,
  input  [9:0]  W0_addr,
  input         W0_en,
                W0_clk,
  input  [31:0] W0_data,
  input  [3:0]  W0_mask
);

  reg [31:0] Memory[0:1023];
  reg        _R0_en_d0;
  reg [9:0]  _R0_addr_d0;
  always @(posedge R0_clk) begin
    _R0_en_d0 <= R0_en;
    _R0_addr_d0 <= R0_addr;
  end // always @(posedge)
  always @(posedge W0_clk) begin
    if (W0_en & W0_mask[0])
      Memory[W0_addr][32'h0 +: 8] <= W0_data[7:0];
    if (W0_en & W0_mask[1])
      Memory[W0_addr][32'h8 +: 8] <= W0_data[15:8];
    if (W0_en & W0_mask[2])
      Memory[W0_addr][32'h10 +: 8] <= W0_data[23:16];
    if (W0_en & W0_mask[3])
      Memory[W0_addr][32'h18 +: 8] <= W0_data[31:24];
  end // always @(posedge)
  assign R0_data = _R0_en_d0 ? Memory[_R0_addr_d0] : 32'bx;
endmodule

module ram32x1024_sync(
  input         clock,
                reset,
                io_wr,
  input  [31:0] io_wdata,
  input         io_rd,
  input  [9:0]  io_addr,
  input         io_byte_en_0,
                io_byte_en_1,
                io_byte_en_2,
                io_byte_en_3,
  output [31:0] io_rdata
);

  wire [31:0] _syncMem_ext_R0_data;
  syncMem_1024x32 syncMem_ext (
    .R0_addr (io_addr),
    .R0_en   (~io_wr & io_rd),
    .R0_clk  (clock),
    .R0_data (_syncMem_ext_R0_data),
    .W0_addr (io_addr),
    .W0_en   (io_wr),
    .W0_clk  (clock),
    .W0_data (io_wdata),
    .W0_mask ({io_byte_en_3, io_byte_en_2, io_byte_en_1, io_byte_en_0})
  );
  assign io_rdata = io_wr ? 32'h0 : _syncMem_ext_R0_data;
endmodule

