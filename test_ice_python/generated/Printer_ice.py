# **********************************************************************
#
# Copyright (c) 2003-2011 ZeroC, Inc. All rights reserved.
#
# This copy of Ice is licensed to you under the terms described in the
# ICE_LICENSE file included in this distribution.
#
# **********************************************************************
#
# Ice version 3.4.2
#
# <auto-generated>
#
# Generated from file `Printer.ice'
#
# Warning: do not edit this file.
#
# </auto-generated>
#

import Ice, IcePy, __builtin__

# Start of module rozprochy
_M_rozprochy = Ice.openModule('rozprochy')
__name__ = 'rozprochy'

# Start of module rozprochy.iiice
_M_rozprochy.iiice = Ice.openModule('rozprochy.iiice')
__name__ = 'rozprochy.iiice'

# Start of module rozprochy.iiice.test
_M_rozprochy.iiice.test = Ice.openModule('rozprochy.iiice.test')
__name__ = 'rozprochy.iiice.test'

if not _M_rozprochy.iiice.test.__dict__.has_key('Printer'):
    _M_rozprochy.iiice.test.Printer = Ice.createTempClass()
    class Printer(Ice.Object):
        def __init__(self):
            if __builtin__.type(self) == _M_rozprochy.iiice.test.Printer:
                raise RuntimeError('rozprochy.iiice.test.Printer is an abstract class')

        def ice_ids(self, current=None):
            return ('::Ice::Object', '::rozprochy::iiice::test::Printer')

        def ice_id(self, current=None):
            return '::rozprochy::iiice::test::Printer'

        def ice_staticId():
            return '::rozprochy::iiice::test::Printer'
        ice_staticId = staticmethod(ice_staticId)

        def _print(self, s, current=None):
            pass

        def __str__(self):
            return IcePy.stringify(self, _M_rozprochy.iiice.test._t_Printer)

        __repr__ = __str__

    _M_rozprochy.iiice.test.PrinterPrx = Ice.createTempClass()
    class PrinterPrx(Ice.ObjectPrx):

        def _print(self, s, _ctx=None):
            return _M_rozprochy.iiice.test.Printer._op_print.invoke(self, ((s, ), _ctx))

        def begin_print(self, s, _response=None, _ex=None, _sent=None, _ctx=None):
            return _M_rozprochy.iiice.test.Printer._op_print.begin(self, ((s, ), _response, _ex, _sent, _ctx))

        def end_print(self, _r):
            return _M_rozprochy.iiice.test.Printer._op_print.end(self, _r)

        def checkedCast(proxy, facetOrCtx=None, _ctx=None):
            return _M_rozprochy.iiice.test.PrinterPrx.ice_checkedCast(proxy, '::rozprochy::iiice::test::Printer', facetOrCtx, _ctx)
        checkedCast = staticmethod(checkedCast)

        def uncheckedCast(proxy, facet=None):
            return _M_rozprochy.iiice.test.PrinterPrx.ice_uncheckedCast(proxy, facet)
        uncheckedCast = staticmethod(uncheckedCast)

    _M_rozprochy.iiice.test._t_PrinterPrx = IcePy.defineProxy('::rozprochy::iiice::test::Printer', PrinterPrx)

    _M_rozprochy.iiice.test._t_Printer = IcePy.defineClass('::rozprochy::iiice::test::Printer', Printer, (), True, None, (), ())
    Printer._ice_type = _M_rozprochy.iiice.test._t_Printer

    Printer._op_print = IcePy.Operation('print', Ice.OperationMode.Normal, Ice.OperationMode.Normal, False, (), (((), IcePy._t_string),), (), None, ())

    _M_rozprochy.iiice.test.Printer = Printer
    del Printer

    _M_rozprochy.iiice.test.PrinterPrx = PrinterPrx
    del PrinterPrx

# End of module rozprochy.iiice.test

__name__ = 'rozprochy.iiice'

# End of module rozprochy.iiice

__name__ = 'rozprochy'

# End of module rozprochy
