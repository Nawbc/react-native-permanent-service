import * as React from 'react';

import { StyleSheet, View } from 'react-native';
import * as PermanentService from 'react-native-permanent-service';

export default function App() {
  React.useEffect(() => {
    PermanentService.initialize({
      script: 'demo.js',
    });

    PermanentService.start('request.js');

    const eventListener = PermanentService.channel.addListener(
      'stop',
      (event) => {
        console.log(event.eventProperty);
      }
    );

    return () => eventListener.remove();
  }, []);

  return <View style={styles.container}>Demo</View>;
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
