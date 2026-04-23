#import "Sunmi.h"

@implementation Sunmi

- (void)isPrinterAvailable:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    resolve(@NO);
}

- (void)connectPrinter:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    resolve(@NO);
}

- (void)printText:(NSString *)text resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    reject(@"UNSUPPORTED", @"Sunmi printing is not supported on iOS", nil);
}

- (void)printQRCode:(NSString *)data resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    reject(@"UNSUPPORTED", @"Sunmi printing is not supported on iOS", nil);
}

- (void)printBarcode:(NSString *)data resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    reject(@"UNSUPPORTED", @"Sunmi printing is not supported on iOS", nil);
}

- (void)printFormattedReceipt:(NSArray *)lines resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    reject(@"UNSUPPORTED", @"Sunmi printing is not supported on iOS", nil);
}

- (void)getPrinterStatus:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    resolve(@(-1));
}

- (void)getPrinterDebugInfo:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    resolve(@{@"serviceBound": @NO, @"initialized": @NO});
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeSunmiSpecJSI>(params);
}

+ (NSString *)moduleName
{
  return @"Sunmi";
}

@end
