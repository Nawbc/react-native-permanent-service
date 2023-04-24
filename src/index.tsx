import { NativeEventEmitter, NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-permanent-service' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const PermanentService = NativeModules.PermanentService
  ? NativeModules.PermanentService
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export interface NotificationOptions {
  title?: string;
  content?: string;
  /**
   * Notification icon name, put the icon into res folder
   */
  icon?: string;
  /**
   * Notification icon type, default `mipmap`
   *
   * @example `drawable`,`mipmap`
   *
   */
  iconType?: string;
}

export interface PermanentServiceOptions {
  /**
   *
   */
  foreground?: boolean;
  /**
   * Auto launch when device bootstrap default true.
   */
  autoLaunch?: boolean;

  script?: string;

  /**
   *
   * Touch the notification to open the deep linking, default null.
   * Open the app by default.
   *
   * @see https://developer.android.com/training/app-links/deep-linking
   * @see https://reactnative.dev/docs/linking
   *
   * @example
   *
   * ```ts
   *  deepLink: "slack://open?team=123456"
   * ```
   */
  deepLink?: string;

  notification?: NotificationOptions;
}

const eventEmitter = new NativeEventEmitter(PermanentService);

export interface ChannelEventEmitter extends NativeEventEmitter {
  once(event: string, callback: (event: any) => void): void;
}

/**
 * NativeEventEmitter extended, add the once method.
 *
 *
 * @example
 * ```ts
 *
 *
 *
 *
 * ```
 *
 */
export const channel: ChannelEventEmitter = Object.create(eventEmitter, {
  once: {
    writable: false,
    value(event: string, callback: (event: any) => void) {
      if (!event && typeof callback !== 'function') {
        throw new Error('once listener arguments error');
      }

      let fired = false;
      let listener = (e: any) => {
        if (!fired) {
          fired = true;
          this.removeListener(event, callback);
          callback(e);
        }
      };
      this.addListener(event, listener);
    },
  },
});

export const backgroundChannel: ChannelEventEmitter = Object.create(
  eventEmitter,
  {
    once: {
      writable: false,
      value(event: string, callback: (event: any) => void) {
        if (!event && typeof callback !== 'function') {
          throw new Error('once listener arguments error');
        }

        let fired = false;
        let listener = (e: any) => {
          if (!fired) {
            fired = true;
            this.removeListener(event, callback);
            callback(e);
          }
        };
        this.addListener(event, listener);
      },
    },
  }
);

const defaultOptions: PermanentServiceOptions = {
  foreground: true,
  autoLaunch: true,
};

export function initialize(
  options?: PermanentServiceOptions
): Promise<boolean> {
  const opts = Object.assign({}, defaultOptions, options);
  return PermanentService.initialize(opts);
}

export function start(script: string): Promise<boolean> {
  return PermanentService.start(script);
}
