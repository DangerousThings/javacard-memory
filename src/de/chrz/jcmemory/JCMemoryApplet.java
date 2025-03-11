package de.chrz.jcmemory;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;

public class JCMemoryApplet extends Applet
{

    private static final byte INS_GET_BATCH = (byte) 0x01;

    private static short[] memPerTot;
    private static short[] memPer;

    private static byte[] batch;

    public static void install(byte bArray[], short bOffset, byte bLength) throws ISOException
    {
        short offset = bOffset;
        offset += (short)(bArray[offset] + 1); // Instance
        offset += (short)(bArray[offset] + 1); // Privileges
        new JCMemoryApplet(bArray, (short)(offset + 1), bArray[offset]).register(bArray, (short)(bOffset + 1), bArray[bOffset]);
    }

    protected JCMemoryApplet(byte[] parameters, short parametersOffset, byte parametersLength)
    {
        // Store maximum persistent memory
        memPerTot = new short[2];
        memPer = JCSystem.makeTransientShortArray((short) 2, JCSystem.CLEAR_ON_DESELECT);
        if (parametersLength >= 4) {
            memPerTot[0] = Util.getShort(parameters, parametersOffset);
            memPerTot[1] = Util.getShort(parameters, (short)(parametersOffset + 2));
        } else {
            // Default value for P71 (167736)
            memPerTot[0] = (short)0x0002;
            memPerTot[1] = (short)0x8F38;
        }

        // Store batch information
        batch = new byte[4];
        if (parametersLength >= 8) {
            Util.arrayCopyNonAtomic(parameters, (short)(parametersOffset + 4), batch, (short) 0, (short) 4);
        } else {
            Util.arrayFillNonAtomic(batch, (short)0, (short) 4, (byte) 0);
        }
    }

    public void process(APDU apdu)
    {
        final byte[] buffer = apdu.getBuffer();
        final byte ins = buffer[ISO7816.OFFSET_INS];

        // Verify command class
        if (!apdu.isISOInterindustryCLA())
        {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
            return;
        }

        short len = 0;
        boolean validInstruction = false;

        // Respond to applet selection
        if (selectingApplet())
        {
            // Measure used memory
            JCSystem.requestObjectDeletion();
            JCSystem.getAvailableMemory(memPer, (short)0, JCSystem.MEMORY_TYPE_PERSISTENT);
            short memTRes = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
            short memTDes = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);
            
            // Encode memory usage into response
            len = Util.setShort(buffer, len, memPer[0]);
            len = Util.setShort(buffer, len, memPer[1]);
            len = Util.setShort(buffer, len, memPerTot[0]);
            len = Util.setShort(buffer, len, memPerTot[1]);
            len = Util.setShort(buffer, len, memTRes);
            len = Util.setShort(buffer, len, memTDes);

            validInstruction = true;
        }
        else
        {
            if(ins == INS_GET_BATCH)
            {
                // Encode stored batch information into response
                len = Util.arrayCopyNonAtomic(batch, (short) 0, buffer, len, (short) 4);
                validInstruction = true;
            }
        }

        if(validInstruction)
        {
            // Send out response
            short le = apdu.setOutgoing();
            len = le > 0 ? (le > len ? len : le) : len;
            apdu.setOutgoingLength(len);
            apdu.sendBytes((short) 0, len);
        }
        else
        {
            ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

}
