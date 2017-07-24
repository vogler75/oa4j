/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.var;

/**
 *
 * @author vogler
 */
public enum VariableType {
     AnyTypeVar (0x00180000),
     Bit32Var (0x00090000),
     Bit64Var (0x004C0000),
     BitVar (0x00040000),
     BlobVar (0x002E0000),
     CharVar (0x000A0000),
     CmdHdlVar (0x00330000),
     ConnHdlVar (0x00320000),
     DpIdentifierVar (0x00150000),
     DynVar (0x000C0000),
     ErrorVar (0x002B0000),
     FileVar (0x00170000),
     FloatVar (0x00070000),
     IntegerVar (0x00050000),
     LangTextVar (0x00280000),
     PointerVar (0x00340000),
     RecHdlVar (0x00310000),
     TextVar (0x00080000),
     TimeVar (0x00030000),
     UIntegerVar (0x00060000),
     LongVar (0x00460000),
     ULongVar (0x00490000),
     NullVar (0xFFFFFFFF),
     Unknown (0);   
     
     VariableType(int n) {
         value=n;
     }
     public final int value;
}
