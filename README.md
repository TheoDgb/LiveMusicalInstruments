# LiveMusicalInstruments

<img src="https://github.com/TheoDgb/LiveMusicalInstruments/blob/main/media/LiveMusicalInstruments_icon.png?raw=true" alt="LiveMusicalInstruments Icon" style="width: 10%;">

## About
Minecraft plugin that lets you play electronic instruments live and broadcast the performance to players in real time.

**Note**:

This plugin is **not intended for most shared Minecraft hosting providers** and may require some basic computer skills (running a Python script, configuring your router, editing a JSON file).

The plugin, originally developed for personal use, is now public and configurable for your own equipment (currently only drums are supported) and your own audio kit.  
Some latency may occur between playing and hearing the audio, due to the transfer of information from your electronic device to the Minecraft server.

Since Minecraft doesn't allow audio streaming, **MIDI is used instead**, combined with an audio kit inside the resource pack (a default kit is already included).

Future updates may include support for additional instruments.

**To work properly, this plugin requires**:
- A resource pack
- A script to detect the exact name of your MIDI-connected module
- A second script to capture and send MIDI data to the plugin (tested on Windows 10)
- Python to run the scripts

The plugin opens a **Web Socket server** to receive MIDI data from the script. You'll therefore need to configure your router to create a **NAT/PAT FTP Data rule** on port **3000 TCP**.

## Installation
1. Connect your device to your PC via MIDI.
2. Run the script `ListDevices.py` to check if your device is detected and to get its exact name.
3. On your router, create a NAT/PAT FTP Data rule for port 3000 TCP.
4. Open the script `LiveMidiToMinecraft.py` in a text editor and configure the following:
    - The public IP of your Minecraft server with `:3000`
    - Your Minecraft username (so the sound propagates from your player in-game)
    - The name of your MIDI device retrieved in step 2
    > By default, the plugin is configured for an Alesis Crimson II drum kit. You can configure your own device by editing `ǸOTE_MAP` (only modify the numbers, not the names). Other parameters can also be customized if needed.
5. Install the `LiveMusicalInstruments.jar` into the `plugins` folder of your Minecraft server and start your server once to generate the config file in `plugins/LiveMusicalInstruments/`.  
Edit if needed (remember to restart the server after each change).
6. Download the `LiveMusicalInstrumentsPack.zip` resource pack.  
For custom kits:
    - In your resource pack, under `assets/minecraft/sounds/drums/`, create your own folder following the same structure and naming convention as `drum_kit_default`.
    - Then, in `assets/minecraft/sounds.json`, add your custom audio files by following the `drum_kit_default` example.
    - Configure your custom kit in `config.yml`.
7. Run in Minecraft `/live start drums`, then launch `LiveMidiToMinecraft.py`.  
A message should appear in Minecraft chat confirming that you are connected and ready to play.  
(All players must have the resource pack installed in order to hear you perform.)