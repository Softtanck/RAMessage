// IRaMessenger.aidl
package com.softtanck.ramessage;
// Once named, the function name cannot be changed, but the function can be added.
// This file is a typical AIDL.
interface IRaMessenger {
  // The methods of the original Handler. Note: this is oneway!!!
  oneway void send(in Message msg);
  // Sync call from handler
  Message sendSync(in Message msg);
}