// @flow

import { NativeEventEmitter, NativeModules } from 'react-native';
const { AppMetrica } = NativeModules;

const appMetricaEventEmitter = new NativeEventEmitter(AppMetrica);

type ActivationConfig = {
    apiKey: string,
    sessionTimeout?: number,
    firstActivationAsUpdate?: boolean,
};

export default {

    /**
     * Starts the statistics collection process.
     * @param {string} apiKey
     */
    activateWithApiKey(apiKey: string) {
        AppMetrica.activateWithApiKey(apiKey);
    },

    /**
     * Starts the statistics collection process using config.
     * @param {object} params
     */
    activateWithConfig(params: ActivationConfig) {
        AppMetrica.activateWithConfig(params);
    },

    /**
     * Sends a custom event message and additional parameters (optional).
     * @param {string} message
     * @param {object} [params=null]
     */
    reportEvent(message: string, params: ?Object = null) {
        AppMetrica.reportEvent(message, params);
    },

    /**
     * Sends error with reason.
     * @param {string} error
     * @param {object} reason
     */
    reportError(error: string, reason: Object) {
        AppMetrica.reportError(error, reason);
    },

    /**
     * Sets the ID of the user profile.
     * @param {string} userProfileId
     */
    setUserProfileID(userProfileId: string) {
        AppMetrica.setUserProfileID(userProfileId);
    },

    /**
     * Report User Profile
     * @param {string} userProfileId
     * @param {any} userProfile
     */
     async reportUserProfile(userProfileId: string, userProfile: any) {
        return await AppMetrica.reportUserProfile(userProfileId, userProfile);
    },

    /**
     * Set callback function for deeplink data
     * Must called after activateWithApiKey() or activateWithConfig()
     * @param {function} callback function to recieve deeplink after SDK init
     * @returns {function} event listener remover
     */
     getDeeplink(callback) {
         // add listner to events from java side....
        const listener = appMetricaEventEmitter.addListener('yandexMetricaDeeplink', (deeplinkState) => {
            if (callback && typeof callback === typeof Function) {
                callback(JSON.parse(deeplinkState));
            }
        });
        // ...then call getting method to start event mechanism
        AppMetrica.getDeferredDeeplink();

        // unregister listener (suppose should be called from componentWillUnmount() )
        return function remove() {
            listener.remove();
        };
    },
};
