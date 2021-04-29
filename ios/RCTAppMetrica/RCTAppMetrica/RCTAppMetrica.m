#import "RCTAppMetrica.h"
#import <YandexMobileMetrica/YandexMobileMetrica.h>

@implementation RCTAppMetrica {

}

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(activateWithApiKey:(NSString *)apiKey)
{
    YMMYandexMetricaConfiguration *configuration = [[YMMYandexMetricaConfiguration alloc] initWithApiKey:apiKey];
    [YMMYandexMetrica activateWithConfiguration:configuration];
}

RCT_EXPORT_METHOD(activateWithConfig:(NSDictionary *)config) {
    YMMYandexMetricaConfiguration *configuration = [[YMMYandexMetricaConfiguration alloc] initWithApiKey:config[@"apiKey"]];
    if (config[@"sessionTimeout"] != (id)[NSNull null]) {
        [configuration setSessionTimeout:[config[@"sessionTimeout"] intValue]];
    }
    if (config[@"firstActivationAsUpdate"] != (id)[NSNull null]) {
        [configuration setHandleFirstActivationAsUpdate:[config[@"firstActivationAsUpdate"] boolValue]];
    }
    [YMMYandexMetrica activateWithConfiguration:configuration];
}

RCT_EXPORT_METHOD(reportEvent:(NSString *)message)
{
    [YMMYandexMetrica reportEvent:message onFailure:NULL];
}

RCT_EXPORT_METHOD(reportEvent:(NSString *)message parameters:(nullable NSDictionary *)params)
{
    [YMMYandexMetrica reportEvent:message parameters:params onFailure:NULL];
}

RCT_EXPORT_METHOD(reportError:(NSString *)message) {
    NSException *exception = [[NSException alloc] initWithName:message reason:nil userInfo:nil];
    [YMMYandexMetrica reportError:message exception:exception onFailure:NULL];
}

RCT_EXPORT_METHOD(setUserProfileID:(NSString *)userProfileID) {
    [YMMYandexMetrica setUserProfileID:userProfileID];
}

RCT_EXPORT_METHOD(reportUserProfile:(NSString *)userProfileID
                  userProfileParam:(NSDictionary *)userProfileParam
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    
    if (userProfileID == NULL) {
        reject(@"-101", @"UserProfileId can't be null", NULL);
    }
    
    [YMMYandexMetrica setUserProfileID:userProfileID];
    
    if (userProfileParam != NULL) {
        YMMMutableUserProfile *profile = [[YMMMutableUserProfile alloc] init];
        
        for (NSString *key in userProfileParam) {
            if ([key isEqual:@"name"]) {
                NSString *value = [userProfileParam valueForKey:key];
                YMMUserProfileUpdate *name = [[YMMProfileAttribute name] withValue:value];
                [profile apply:name];
            } else if ([key isEqual:@"gender"]) {
                NSString *value = [userProfileParam valueForKey:key];
                YMMUserProfileUpdate *gender = NULL;

                if ([value isEqual: @"male"]) {
                    gender = [[YMMProfileAttribute gender] withValue:YMMGenderTypeMale];
                } else if ([value isEqual: @"female"]) {
                    gender = [[YMMProfileAttribute gender] withValue:YMMGenderTypeFemale];
                } else {
                    gender = [[YMMProfileAttribute gender] withValue:YMMGenderTypeOther];
                }
                
                [profile apply:gender];
            } else if ([key isEqual:@"birthDate"]) {
                NSNumber *value = [userProfileParam valueForKey:key];
                
                if (value != NULL) {
                    YMMUserProfileUpdate *birthDate = [[YMMProfileAttribute birthDate] withAge:value.intValue];
                    [profile apply:birthDate];
                }

            } else if ([key isEqual:@"notificationsEnabled"]) {
                BOOL value = [userProfileParam objectForKey:key];
                YMMUserProfileUpdate *notificationsEnabled = [[YMMProfileAttribute notificationsEnabled] withValue:value];
                [profile apply:notificationsEnabled];
            } else {
                YMMUserProfileUpdate *customAttribute = NULL;
                
                if ([[userProfileParam valueForKey:key] isKindOfClass:[NSString class]]) {
                    NSString *value = [userProfileParam valueForKey:key];
                    customAttribute = [[YMMProfileAttribute customString:key] withValue:value];
                } else if ([[userProfileParam valueForKey:key] isKindOfClass:[NSNumber class]]) {
                    NSNumber *value = [userProfileParam valueForKey:key];
                    
                    if (([value isEqual:[NSNumber numberWithBool:YES]] || [value isEqual:[NSNumber numberWithBool:NO]])
                        && value.intValue && value.intValue >= 0 && value.intValue <= 1) {
                        customAttribute = [[YMMProfileAttribute customBool:key] withValue:value.boolValue];
                    } else {
                        customAttribute = [[YMMProfileAttribute customNumber:key] withValue:value.intValue];
                    }
                }

                if (customAttribute != NULL) {
                    [profile apply:customAttribute];
                }
            }
        }
        
        if ([profile updates].count > 0) {
            [YMMYandexMetrica reportUserProfile:[profile copy] onFailure:^(NSError *error) {
                reject(@"-103", error.localizedDescription, error);
            }];
            
            resolve(userProfileParam);
        } else {
            reject(@"-102", @"Valid keys not found", NULL);
        }
    }
    
}

@end
