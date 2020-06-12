# lostik

LoStik is a simple USB LoRaWAN dongle build by Ronoth. This Clojure library provides a set of functions to connect to the LoStik over its USB emulated UART, configure the device and connect to a LoRaWAN network to send and receive data.

The package is intended to be used from the repl.

## Usage

The package is intended to be used from the repl.

## Example Usage

The easiest way to get started is to checkout the repository and open a Clojure repl. The `lostik.repl` already imports the required modules.

```clojure
;; create a lora instance to handle transmissions
(def lora (l/new-lora "/dev/ttyUSB0"))
;; enable the device
(l/enable! lora)
;; join a lora network using the otaa method
(l/join! lora app-eui app-key device-eui)
;; encode the hex string "abc" to byte sequence (ascii) and send it over the network
(send-data! lora (u/str->bytes "abc"))
```

The `send-data!` function does not take care of byte order! It will send the bytes as they are in the sequence. If you need to encode e.g. an IEEE float value you have to take care of the correct byte order yourself!


### Custom Message Handler

The `enable!` function sets up a primitive handler that takes all messages received from the device, no matter if they messages indicating the device state, the state of the lora connection or responses received over lora. For a real application you should provide a custom handler: `(enable! lora handler-fn)`. The `lora-msg-handler` is a good starting point as an example how such a handler could look like.

## License

Copyright © 2020 Clockworks UG (haftungsbeschränkt)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
