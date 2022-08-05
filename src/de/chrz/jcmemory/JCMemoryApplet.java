package de.chrz.jcmemory;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;

public class JCMemoryApplet extends Applet
{

	public static void
	install(byte[] info, short off, byte len)
	{
		final JCMemoryApplet applet = new JCMemoryApplet();
		applet.register();
	}

	protected
	JCMemoryApplet()
	{

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

            short[] memPer = new short[2];
			JCSystem.getAvailableMemory(memPer, (short)0, JCSystem.MEMORY_TYPE_PERSISTENT);
            short memTRes = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
            short memTDes = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);
            buffer[len++] = (byte)(memPer[0] & 0xFF);
            buffer[len++] = (byte)((memPer[0] >> 8) & 0xFF);
			buffer[len++] = (byte)(memPer[1] & 0xFF);
            buffer[len++] = (byte)((memPer[1] >> 8) & 0xFF);
            buffer[len++] = (byte)(memTRes & 0xFF);
            buffer[len++] = (byte)((memTRes >> 8) & 0xFF);
            buffer[len++] = (byte)(memTDes & 0xFF);
            buffer[len++] = (byte)((memTDes >> 8) & 0xFF);

            len = le > 0 ? (le > len ? len : le) : len;
            apdu.setOutgoingLength(len);
            apdu.sendBytes((short)0, len);

			return;
		}
	}

}
