'use strict';
import {
  NativeAppEventEmitter,
  DeviceEventEmitter,
  NativeModules,
  Platform
} from 'react-native';
let NearbyNative = NativeModules.Nearby;

class Nearby {

  /**
   * example config:
   * const nearbyConfig: {
   *    message: `Hello there from user:${userId}`
   * }
   */
  constructor (config) {
    const { message } = config;

    if (typeof message === 'undefined')
      throw 'Please provide a message in your config object.';

    this.nearby = NearbyNative;
    this.handlers = {};
    this.nearbyHelperHandlers = {};
    this.Emitter = Platform.OS === 'ios' ? NativeAppEventEmitter : DeviceEventEmitter;

    this.deviceEventSubscription = this.Emitter.addListener(
      'nearbySubscribe', this._handleEvent.bind(this)
    );
    this.nearby.init(config.message);
  }

  _handleEvent(event) {
    if (this.handlers.hasOwnProperty(event.method)) {
      this.handlers[event.method](
        (event.hasOwnProperty('message')) ? event.message : null
      );
    }

    if (this.nearbyHelperHandlers) this.nearbyHelperHandlers(event);
  }

  /**
   * onFound: Nearby user found
   */
  onFound(event, handler) {
    this.handlers[event] = handler;
  }

  /**
   * onHelperEvents: userful callback that come back from the Nearby Native Module
   */
  onHelperEvents(handler) {
    this.nearbyHelperHandlers = handler;
  }
}

module.exports = Nearby;