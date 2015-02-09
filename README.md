# mmm
Udisks mount wrapper for i3 written on Java

This script uses Udisks to identify and mount volumes.
Initially written for i3WM, uses DMENU.

Features:
-----
- Fast
- It can exclude drive names. F.e. not to show /dev/sda and all its childs in the list of drives, so /dev/sda1, /dev/sda2 and so on will 
not be shown. See config file for more info.
- Adheres UNIX philosophy - it is simple, very simple.
- I dunno

Compilation:
-----
Source files lies under `src` directory. Compile them using `javac` command and make `.jar` executable package.

`src` directory contains already precompiled `.class` files.

Installation:
-----
If you don't want to compile the source, just get already compiled and packaged `mmm.jar` file. 

Launch it like this:
```
java -jar mmm.jar
```

The point is to integrate this program to your i3WM config file or just to make keyboard launching shortcut.
Place this to your `~/.i3/config` file:
```
bindsym $mod+M exec --no-startup-id java -jar ~/place_to_the_script/mmm.jar
```
and refresh i3 configuration. After this you will be able to mount drives with `$mod+M` shortcut. 

Also it is possible to make a symlink from your `/usr/bin/` directory. 

Usage and config file:
-----
To mount a volume just select it.

To unmount a volume select unmount option.

All volumes are mounted to standard UDISKS volume mount path - `/media/volume_name`.

You can create a configuration file in the `~/.config/mmm.conf` directory. Config file lines are:

- no-mount=sda,sdb - Place this to exclude all /dev/sda and /dev/sdb devices. All partitions within these devices will also be excluded.
It is useful if /dev/sda - your hard drive and you don't want to see it on the list.
- no-notify=0 - Place 1 to disable notifications.


Obsolete versions:
-----
There is also obsolete version written on bash. It lacks many features. Don't use it.

Requires:
-----
- DMENU
- UDISKS
- LSBLK
- JAVA Virtual Machine
- BASH
- notify-send (optional)

It won't work with Udisks2. Only Udisks for now.
