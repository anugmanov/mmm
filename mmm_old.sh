#!/bin/bash
#Works with any version of LSBLK
#udisks for mounting
#Using DMENU 
#i3 for best experience
#---------------------------
#Author: __nugman           |
#SCRIPT FOR EASY MONITORING | 
#AND MOUNTING PURPOSES 	    |
#IN TILING WM's		    |
#---------------------------

COUNT=0 #C of drives
DRIVES="" #string for dmenu
KEEP_LOGS=0 #1 for keeping logs - dev purposes
CONFIG_PATH="$HOME/.config/MMM.conf"

NOTIFY=1
DMNT=0 #dismount option
declare -a WO #without these
mount () {
	DRV=$(lsblk -ln) #GET all drives LSBLK
	IFS=$'\n'

	for line in $DRV;
	do
		parse_string $line
	done

	RES=$(echo -e $DRIVES | dmenu -fn "-xos4-terminus-*-*-*-*-12-*-*-*-*-*-*-*" -i -l $COUNT)

	if [ -z "$RES" ];then 
		exit 0 
	fi

	D=$(echo $RES | grep -E -o -e "DISMOUNT")
	if [ -n "$D" ]; then #If we gonna dismount
		RES="/dev/$(echo $RES | grep -E -o -e "sd.[0-9]")"
		O=$(udisks --unmount $RES)

		if (( NOTIFY )); then
			notify-send "$RES successfully dismounted. Yay!" 
		fi
	else #Else - mount
		RES="/dev/$(echo $RES | grep -E -o -e "sd.[0-9]")"
		O=$(udisks --mount $RES)

		if (( NOTIFY )); then
			notify-send $O
		fi

	fi
}

parse_string () {
	
	NAME=$(echo $1 | grep -E -o -e "sd.[0-9]")
	FULL_S=$(echo $1 | grep -E -e "sd.[0-9]")

		
	#-----PARSE TO EXCLUDE
	NN=${#WO[@]}
	for (( i=1;i<=$NN;i++ )); do
		if [[ ${WO[$i]} == ${NAME%?} ]];then
			NAME=""
		fi	
	done
	#-------------------

	if [ -n "$NAME" ]; 
	then
		NAME=$FULL_S #revert back to full string
		if (( $DMNT )); then
			NAME="${NAME}\nDISMOUNT_${NAME}\n"
			DRIVES="${DRIVES}${NAME}"
			(( COUNT+=2 ))
		else
			DRIVES="${DRIVES}${NAME}\n"
			(( COUNT++ ))
		fi

	fi
	
}

show_help () {
	#show-help-msg
echo -e "Usage: MMM.sh [][-h]
	
	ABOUT:
	----------------------------------------------------
	For easy mounting purposes. Shows drives in a dmenu. 
	Using lsblk and udisks.
	No permissions for mountins USB drives required.
	For further reading about mounting mechanism
	see \"man udisks\".
	---------------------------------------------------

	CONFIG FILE:
	Config files usually placed in ~/.config/MMM.conf
	What you can add there:
		no-mount="sda,sdb" #do not mount ALL sdA and sdB devices.
				   #so sda1, sda2, sdb1 etc. will not be 
				   #shown in menu 
		
		no-notify=1	   #Specify to turn off notifications

		dismount=1	   #Put dismount option after drive 
	     			   #in menu

	That's all! Simple to stupidity.

	MMM.sh			01.07.2015			Author: __nugman"
}
read_config () {
	if [ -f $CONFIG_PATH ];
	then
		CONF=$(cat $CONFIG_PATH)

		for line in $CONF;
		do
			STR=$(echo $line | grep -o -e "^.*=")
			echo $STR
			STR=${STR%?}
		
			#---parse args case
			case $STR in
				"no-mount")
					exclude_drives $line
					#do-smth-here
					;;
				"no-notify")
					N=$(echo $line | grep -e ".$")
					if [[ "$N" == 1 ]]; then
						NOTIFY=0
					fi
					;;
				"dismount")
					N=$(echo $line | grep -o -e ".$")
					if [[ "$N" == 1 ]]; then
						DMNT=1
					fi
					;;
			esac
			#------------------

		done
	fi
}

exclude_drives () {
	DRVS=$(echo $1 | grep -o -e "\".*\"")
	DRVS=${DRVS:1: $(( ${#DRVS}-2 ))} #delete first and last chars

	#String will be like here:
	#sda,sdb,sdc,sdd
	#1.3.5.7
	LEN=${#DRVS}
	A=1
	B=3
	NN=$(( LEN/3 ))
	for (( i=1;i<=$NN;i++ )); do
		WO[$i]=$(echo $DRVS | cut -c "$A-$B")
		((A += 4 )) #With comma
		((B += 4 ))
	done

}

main () {
	read_config
	mount
	exit 0
}

case $1 in
	"-h")
		show_help
		;;
	*)
		main
		;;
esac
