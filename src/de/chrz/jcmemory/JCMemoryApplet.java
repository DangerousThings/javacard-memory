package de.chrz.jcmemory;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;

public class JCMemoryApplet extends Applet
{

    private short[] memPerTot;

    public static void
    install(byte bArray[], short bOffset, byte bLength) throws ISOException
    {
        short offset = bOffset;
        offset += (short)(bArray[offset] + 1); // Instance
        offset += (short)(bArray[offset] + 1); // Privileges
        new JCMemoryApplet(bArray, (short)(offset + 1), bArray[offset]).register(bArray, (short)(bOffset + 1), bArray[bOffset]);
    }

    protected
    JCMemoryApplet(byte[] parameters, short parametersOffset, byte parametersLength)
    {
        memPerTot = new short[2];
        if (parametersLength >= 4) {
            memPerTot[0] = Util.getShort(parameters, parametersOffset);
            memPerTot[1] = Util.getShort(parameters, (short)(parametersOffset + 2));
        } else {
            // Default value for P71 (167736)
            memPerTot[0] = (short)0x0002;
            memPerTot[1] = (short)0x8F38;
        }
    }

    public void
    process(APDU apdu)
    {
        final byte[] buffer = apdu.getBuffer();
        final byte ins = buffer[ISO7816.OFFSET_INS];

        if (!apdu.isISOInterindustryCLA()) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
            return;
        }

        if (selectingApplet()) {
            short len, le;
            le = apdu.setOutgoing();
            len = 0;

            JCSystem.requestObjectDeletion();
            short[] memPer = new short[2];
            JCSystem.getAvailableMemory(memPer, (short)0, JCSystem.MEMORY_TYPE_PERSISTENT);
            short memTRes = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
            short memTDes = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);
            
            len = Util.setShort(buffer, len, memPer[0]);
            len = Util.setShort(buffer, len, memPer[1]);
            len = Util.setShort(buffer, len, memPerTot[0]);
            len = Util.setShort(buffer, len, memPerTot[1]);
            len = Util.setShort(buffer, len, memTRes);
            len = Util.setShort(buffer, len, memTDes);

            len = le > 0 ? (le > len ? len : le) : len;
            apdu.setOutgoingLength(len);
            apdu.sendBytes((short)0, len);

            return;
        }
    }

}
