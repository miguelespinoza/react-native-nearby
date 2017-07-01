'use strict';
import React, {Component} from 'react';
import {Text, View, StyleSheet} from 'react-native';
import Nearby from 'react-native-nearby';

export default class Main extends Component {

  constructor(props) {
    super(props);

    const nearbyConfig = {
      message: 'Hello there'
    };
    this.nearby = new Nearby(nearbyConfig);
    this.state = {
      nearbyData: ""
    };
  }

  componentDidMount() {
    this.nearby.onFound((event) => {
      const { method, message } = event;
      this.setState({
        nearbyData: this.state.nearbyData + `(${method}) : {\n${message}\n}\n`
      });
    });

    this.nearby.onHelperEvents((event) => {
      const { method, message } = event;
      this.setState({
        nearbyData: this.state.nearbyData + `(h_${method}) : {\n${message}\n}\n`
      });
    });
  }



  render() {
    return (
      <View style={styles.container}>
        <Text>Hello World</Text>
        <Text>{this.state.nearbyData}</Text>
       </View>
    );
  }
}

var styles = StyleSheet.create({
  container: {
    flex: 1,
  }
});