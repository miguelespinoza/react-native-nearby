import React, { Component } from 'react';
import {
  AppRegistry,
} from 'react-native';
import Main from './main';

export default class example extends Component {
  render() {
    return (
      <Main />
    );
  }
}

AppRegistry.registerComponent('example', () => example);
