/**
 *  Z-Wave Siren Switch Generic
 *  Build 2018111404
 *
 *  Adapted from Z-Wave Switch Generic
 *
 *  Copyright 2018 Jordan Markwell
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  ChangeLog:
 *      
 *      20181114:
 *          01: Applied updates based on Z-Wave Switch Generic.
 *          02: Rolling back custom device type / cloud incompatible changes.
 *          03: Modified deviceJoinName.
 *          04: Removed device fingerprints. This device handler should be selected manually and not automatically.
 *
 *      20170829:
 *          01: Added code for non-GE model compatibility back in
 *          02: Updated based on updated Z-Wave Switch Generic to include CRC16 encapsulation
 *
 *      Earlier:
 *          Creation
 */
metadata {
    definition(name: "Z-Wave Siren Switch Generic", namespace: "jmarkwell", author: "Jordan Markwell") {
        capability "Actuator"
        capability "Alarm"
        capability "Health Check"
        capability "Light"
        capability "Polling"
        capability "Refresh"
        capability "Sensor"
        capability "Switch"
    }
    
    simulator {
        status "both": "command: 2003, payload: FF"
        status "off": "command: 2003, payload: 00"
        
        reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
        reply "200100,delay 100,2502": "command: 2503, payload: 00"
    }
    
    tiles(scale: 2) {
        multiAttributeTile(name: "alarm", type: "generic", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.alarm", key: "PRIMARY_CONTROL") {
                attributeState "both", label: 'alarm!', action: "alarm.off", icon: "st.alarm.alarm.alarm", backgroundColor: "#e86d13"
                attributeState "off", label: 'off', action: "alarm.siren", icon: "st.alarm.alarm.alarm", backgroundColor: "#ffffff"
            }
        }
        
        standardTile("refresh", "device.alarm", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
        }
        
        main "alarm"
        details(["alarm", "refresh"])
    }
}

def installed(){
    // Device-Watch pings if no events received for checkInterval (32min = 2 * 15min + 2min lag time) duration.
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
    response( refresh() )
}

def updated(){
    // Device-Watch pings if no events received for checkInterval (32min = 2 * 15min + 2min lag time) duration.
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}

def getCommandClassVersions() {
    [
        0x20: 1,  // basic
        0x56: 1,  // CRC16Encap
        0x70: 1,  // configuration
    ]
}

def parse(String description) {
    def result = null
    def cmd = zwave.parse(description, commandClassVersions)
    
    if (cmd) {
        result = createEvent(zwaveEvent(cmd))
    }
    
    if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
        result = [result, response(zwave.basicV1.basicGet())]
        log.debug "hailed: requesting state update"
    } else {
        log.debug "parse returned: ${result?.descriptionText}"
    }
    
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    [name: "alarm", value: cmd.value ? "both" : "off"]
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
    [name: "alarm", value: cmd.value ? "both" : "off"]
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    [name: "alarm", value: cmd.value ? "both" : "off"]
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
    [name: "hail", value: "hail", descriptionText: "alarm button was pressed", displayed: false]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    log.debug "manufacturerId:   $cmd.manufacturerId"
    log.debug "manufacturerName: $cmd.manufacturerName"
    log.debug "productId:        $cmd.productId"
    log.debug "productTypeId:    $cmd.productTypeId"
    
    def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
    updateDataValue("MSR", msr)
    updateDataValue("manufacturer", cmd.manufacturerName)
    
    createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
    def versions = commandClassVersions
    def version = versions[cmd.commandClass as Integer]
    def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
    def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
    if (encapsulatedCommand) {
        zwaveEvent(encapsulatedCommand)
    }
}

// This handles unknown commands.
def zwaveEvent(physicalgraph.zwave.Command cmd) {
    [:]
}

def on() {
    delayBetween([
        zwave.basicV1.basicSet(value: 0xFF).format(),
        zwave.basicV1.basicGet().format()
    ])
}

def both() {
    on()
}

def siren() {
    on()
}

def strobe() {
    on()
}

def off() {
    delayBetween([
        zwave.basicV1.basicSet(value: 0x00).format(),
        zwave.basicV1.basicGet().format()
    ])
}

def refresh() {
    def commands = []
    commands << zwave.basicV1.basicGet().format()
    if (getDataValue("MSR") == null) {
        commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
    }
    delayBetween(commands)
}

// ping() is used used by Device Watch.
def ping() {
    refresh()
}

def poll() {
    refresh()
}
