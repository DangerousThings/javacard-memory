#!/usr/bin/env python3

import argparse
from smartcard.System import readers

# Parse arguments
parser = argparse.ArgumentParser(description = 'Read javacard memory')
parser.add_argument('-l', '--list-readers', action='store_true', dest='listreaders', 
    help='list available PC/SC readers')
parser.add_argument('-r', '--reader', nargs='?', dest='reader', type=int, 
    const=0, default=0, 
    required=False, help='index of the PC/SC reader to use (default: 0)')
args = parser.parse_args()

if(args.listreaders):
    # List readers
    redlist = readers()
    if(len(redlist) == 0):
        print('warning: No PC/SC readers found')
        exit(1)
    redlist.sort(key=str)
    print('info: Available PC/SC readers (' + str(len(redlist)) + '):')
    for i, reader in enumerate(redlist):
        print(str(i) + ': ' + str(reader))
else:
    # Read usage
    redlist = readers()
    if(len(redlist) == 0):
        print('error: No PC/SC readers found')
        exit(1)
    if(args.reader < 0 or args.reader >= len(redlist)):
        print('error: Specified reader index is out of range')
        exit(1)
    redlist.sort(key=str)
    red = redlist[args.reader]
    print('info: Using reader ' + str(args.reader) + ': ' + str(red))

    connection = red.createConnection()
    connection.connect()
    # Select the applet
    print('info: Sending applet selection')
    data, sw1, sw2 = connection.transmit(
        [0x00, 0xA4, 0x04, 0x00, 0x0C, 0xA0, 0x00, 0x00, 0x08, 0x46, 0x6D, 0x65, 0x6D, 0x6F, 0x72, 0x79, 0x01])
    if(sw1 == 0x90 and sw2 == 0x00):
        print('success: Applet selected, card response is ok')
        # Parse response
        memPer = int.from_bytes(data[0:4], 'big')
        memPerTot = int.from_bytes(data[4:8], 'big')
        memPerQ = (memPer / memPerTot) * 100.0
        memTRes = int.from_bytes(data[8:10], 'big')
        memTDes = int.from_bytes(data[10:12], 'big')
        memTQ = min(1.0, (((memTRes + memTDes) / 2.0) / 4096.0)) * 100.0
        print('Available memory:')
        print('PERSISTENT:         ' + str(memPer) + ' bytes of ' + str(memPerTot) + ' bytes (' + str(int(memPerQ)) + '% free)')
        print('TRANSIENT_RESET:    ' + str(memTRes) + ' bytes')
        print('TRANSIENT_DESELECT: ' + str(memTDes) + ' bytes')
        print('TRANSIENT free: ' + str(int(memTQ)) + '%')

        print('info: Sending batch command')
        data, sw1, sw2 = connection.transmit(
            [0x00, 0x01, 0x00, 0x00, 0x00])
        if(sw1 == 0x90 and sw2 == 0x00):
            print('success: Batch info, card response is ok')
            print('info: Batch ' + bytes(data).hex())
        else:
            print('error: Version card response: ' + f'{sw1:02x}' + ' ' + f'{sw2:02x}')

        print('info: Sending version command')
        data, sw1, sw2 = connection.transmit(
            [0x80, 0xF4, 0x99, 0x99, 0x00])
        if(sw1 == 0x90 and sw2 == 0x00):
            print('success: Version info, card response is ok')
            print('info: Applet ' + str(data[0]) + '.' + str(data[1]) + ', Build ' + str(data[2]) + '.' + str(data[3]) + '.' + str(data[4]))
        else:
            print('error: Version card response: ' + f'{sw1:02x}' + ' ' + f'{sw2:02x}')
    else:
        print('error: Card response: ' + f'{sw1:02x}' + ' ' + f'{sw2:02x}')
    connection.disconnect()

