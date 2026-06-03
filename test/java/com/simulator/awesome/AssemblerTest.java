package com.simulator.awesome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AssemblerTest {

    private Assembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new Assembler();
        assembler.currentFile = "<test>";
    }

    private String[] assemble(String... lines) {
        assembler.input_arr = lines;
        return assembler.convertToMachineCode();
    }

    // --- HLT ---

    @Test
    void hlt_encodesCorrectly() {
        String[] result = assemble("HLT");
        assertEquals(1, result.length);
        assertEquals("0000000000000000", result[0]);
    }

    // --- LDR: opcode=000001, r=2bits, ix=2bits, i=1bit, addr=5bits ---

    @Test
    void ldr_r0_ix0_addr10_noIndirect() {
        // LDR 0,0,10 -> 000001 00 00 0 01010
        String[] result = assemble("LDR 0,0,10");
        assertEquals("0000010000001010", result[0]);
    }

    @Test
    void ldr_r1_ix0_addr5_noIndirect() {
        // LDR 1,0,5 -> 000001 01 00 0 00101
        String[] result = assemble("LDR 1,0,5");
        assertEquals("0000010100000101", result[0]);
    }

    @Test
    void ldr_withIndirect() {
        // LDR 0,0,10,1 -> 000001 00 00 1 01010
        String[] result = assemble("LDR 0,0,10,1");
        assertEquals("0000010000101010", result[0]);
    }

    // --- STR: same encoding layout as LDR ---

    @Test
    void str_r0_ix0_addr0() {
        // STR 0,0,0 -> 000010 00 00 0 00000
        String[] result = assemble("STR 0,0,0");
        assertEquals("0000100000000000", result[0]);
    }

    // --- AIR: opcode=000110, r=2bits, ix=00(ignored), i=0, addr=5bits ---

    @Test
    void air_r0_imm5() {
        // AIR 0,5 -> 000110 00 00 0 00101
        String[] result = assemble("AIR 0,5");
        assertEquals("0001100000000101", result[0]);
    }

    @Test
    void air_r1_imm31() {
        // AIR 1,31 -> 000110 01 00 0 11111
        String[] result = assemble("AIR 1,31");
        assertEquals("0001100100011111", result[0]);
    }

    // --- NOT: opcode=011001, rx=2bits, ry=00(ignored), 000000 ---

    @Test
    void not_r1() {
        // NOT 1 -> 011001 01 00 000000
        String[] result = assemble("NOT 1");
        assertEquals("0110010100000000", result[0]);
    }

    // --- AND: opcode=010111, rx=2bits, ry=2bits, 000000 ---

    @Test
    void and_rx0_ry1() {
        // AND 0,1 -> 010111 00 01 000000
        String[] result = assemble("AND 0,1");
        assertEquals("0101110001000000", result[0]);
    }

    // --- ORR: opcode=011000 ---

    @Test
    void orr_rx1_ry2() {
        // ORR 1,2 -> 011000 01 10 000000
        String[] result = assemble("ORR 1,2");
        assertEquals("0110000110000000", result[0]);
    }

    // --- MLT: opcode=010100 ---

    @Test
    void mlt_rx0_ry2() {
        // MLT 0,2 -> 010100 00 10 000000
        String[] result = assemble("MLT 0,2");
        assertEquals("0101000010000000", result[0]);
    }

    // --- Direct binary passthrough ---

    @Test
    void binaryPassthrough_startsWith0() {
        String line = "0000000001100100";
        String[] result = assemble(line);
        assertEquals(line, result[0]);
    }

    @Test
    void binaryPassthrough_startsWith1() {
        String line = "1111111111111111";
        String[] result = assemble(line);
        assertEquals(line, result[0]);
    }

    // --- Comment handling ---

    @Test
    void lineComment_skipped() {
        String[] result = assemble("# this is a comment", "HLT");
        assertEquals(1, result.length);
        assertEquals("0000000000000000", result[0]);
    }

    @Test
    void inlineComment_stripped() {
        String[] result = assemble("HLT ; halt the machine");
        assertEquals(1, result.length);
        assertEquals("0000000000000000", result[0]);
    }

    @Test
    void binaryWithInlineComment_stripped() {
        String[] result = assemble("0000000000000000; Local 0");
        assertEquals("0000000000000000", result[0]);
    }

    // --- Null / blank line handling (issue #122) ---

    @Test
    void nullLastLine_doesNotThrowNpe() {
        // Files.lines().count() counts the trailing newline as an extra line; that slot is null
        assertDoesNotThrow(() -> assemble("HLT", null),
            "null line at end of input_arr must not throw NullPointerException");
    }

    @Test
    void nullLastLine_producesCorrectOutput() {
        String[] result = assemble("HLT", null);
        assertEquals(1, result.length, "null trailing line must be skipped");
        assertEquals("0000000000000000", result[0]);
    }

    @Test
    void blankLastLine_isSkipped() {
        String[] result = assemble("HLT", "");
        assertEquals(1, result.length, "blank trailing line must be skipped");
    }

    @Test
    void whitespaceOnlyLine_isSkipped() {
        String[] result = assemble("HLT", "   ");
        assertEquals(1, result.length, "whitespace-only line must be skipped");
    }

    // --- Multi-instruction program ---

    @Test
    void multiInstruction_correctOrder() {
        String[] result = assemble("LDR 0,0,5", "HLT");
        assertEquals(2, result.length);
        // LDR 0,0,5 -> 000001 00 00 0 00101
        assertEquals("0000010000000101", result[0]);
        assertEquals("0000000000000000", result[1]);
    }
}
