(ns lostik.util
  "Utilities for lostik.")

(defn str->bytes
  "Convert a string to a byte array (ASCII)."
  [s]
  (bytes (byte-array (map (comp byte int) s))))

(defn bytes->str
  "Convert byte array to string (ASCII)."
  [b]
  (apply str (map char b)))

(defn w-crlf
  "Append CRLF to a string.
  **Hint**: CRLF is the line termination sequence used by lostik."
  [s]
  (str s "\u000d\u000a"))

(defn bytes->hexs
  "Convert byte sequence to hex string.

  Creates a two character hex value for each byte and assembles a string from
  all bytes in the sequence.

  **Important**: Takes the bytes as they come. Does not care about endianess."
  [bs]
  (apply str (map (fn [b] (format "%02x" b)) bs)))
