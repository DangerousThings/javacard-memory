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
        [0x00, 0xA4, 0x04, 0x00, 0x07, 0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0x01])
    if(sw1 == 0x90 and sw2 == 0x00):
        print('success: Applet selected, card response is ok')
        # Parse response
        memPer = (data[2] | (data[3] << 8)) | (data[0] | (data[1] << 8)) << 16
        memTRes = data[4] | (data[5] << 8)
        memTDes = data[6] | (data[7] << 8)
        print('Available memory:')
        print('PERSISTENT:         ' + str(memPer) + ' bytes')
        print('TRANSIENT_RESET:    ' + str(memTRes) + ' bytes')
        print('TRANSIENT_DESELECT: ' + str(memTDes) + ' bytes')
    else:
        print('error: Card response: ' + f'{sw1:02x}' + ' ' + f'{sw2:02x}')
    connection.disconnect()

