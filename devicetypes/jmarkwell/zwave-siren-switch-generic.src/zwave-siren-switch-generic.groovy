/**
 *  Z-Wave Siren Switch Generic
 *  Build 2018111401
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
 *
 *      20170829:
 *          01: Added code for non-GE model compatibility back in
 *          02: Updated based on updated Z-Wave Switch Generic to include CRC16 encapsulation
 *
 *      Earlier:
 *          Creation
 */
metadata {
    definition(name: "Z-Wave Siren Switch Generic", namespace: "jmarkwell", author: "Jordan Markwell", ocfDeviceType: "oic.d.switch", runLocally: true, minHubCoreVersion: '000.019.00012', executeCommandsLocally: true) {
        capability "Actuator"
        capability "Alarm"
        capability "Health Check"
        capability "Light"
        capability "Polling"
        capability "Refresh"
        capability "Sensor"
        capability "Switch"
        
        fingerprint inClusters: "0x25", deviceJoinName: "Z-Wave Switch"
        
        fingerprint mfr: "001A", prod: "5244", deviceJoinName: "Eaton RF Receptacle"
        fingerprint mfr: "001A", prod: "534C", model: "0000", deviceJoinName: "Eaton RF Master Switch"
        fingerprint mfr: "001A", prod: "5352", model: "0000", deviceJoinName: "Eaton RF Accessory Switch"
        fingerprint mfr: "001A", prod: "5354", model: "0003", deviceJoinName: "Eaton RF Appliance Plug-In Module"
        
        fingerprint mfr: "0063", prod: "4F50", model: "3031", deviceJoinName: "GE Plug-in Outdoor Switch"
        fingerprint mfr: "0063", prod: "4F50", model: "3032", deviceJoinName: "GE Plug-in Outdoor Switch"
        fingerprint mfr: "0063", prod: "5250", model: "3130", deviceJoinName: "GE Plug-in Outdoor Switch"
        
        fingerprint mfr: "001D", prod: "0301", model: "0334", deviceJoinName: "Leviton 15A Switch"
        fingerprint mfr: "001D", prod: "0F01", model: "0334", deviceJoinName: "Leviton 5A Incandescent Switch"
        fingerprint mfr: "001D", prod: "1603", model: "0334", deviceJoinName: "Leviton 15A Split Duplex Receptacle"
        fingerprint mfr: "001D", prod: "1A02", model: "0334", deviceJoinName: "Leviton Appliance Module"
        fingerprint mfr: "001D", prod: "1C02", model: "0334", deviceJoinName: "Leviton Switch"
        fingerprint mfr: "001D", prod: "1D04", model: "0334", deviceJoinName: "Leviton Outlet"
        fingerprint mfr: "001D", prod: "3401", model: "0001", deviceJoinName: "Leviton Switch" //Leviton DZ15S
        
        fingerprint mfr: "011A", prod: "0101", model: "0102", deviceJoinName: "Enerwave On/Off Switch"
        fingerprint mfr: "011A", prod: "0101", model: "0603", deviceJoinName: "Enerwave Duplex Receptacle"
        
        fingerprint mfr: "0039", prod: "4952", model: "3036", deviceJoinName: "Honeywell Z-Wave In-Wall Smart Switch"
        fingerprint mfr: "0039", prod: "4952", model: "3037", deviceJoinName: "Honeywell Z-Wave In-Wall Smart Toggle Switch"
        fingerprint mfr: "0039", prod: "4952", model: "3133", deviceJoinName: "Honeywell Z-Wave In-Wall Tamper Resistant Duplex Receptacle"
        fingerprint mfr: "0039", prod: "4F50", model: "3032", deviceJoinName: "Honeywell Z-Wave Plug-in Outdoor Smart Switch"
        fingerprint mfr: "0039", prod: "5052", model: "3033", deviceJoinName: "Honeywell Z-Wave Plug-in Switch (Dual Outlet)"
        fingerprint mfr: "0039", prod: "5052", model: "3038", deviceJoinName: "Honeywell Z-Wave Plug-in Switch"
        
        fingerprint mfr: "014F", prod: "5257", model: "3033", deviceJoinName: "GoControl Wall Relay Switch"
        fingerprint mfr: "014F", prod: "5753", model: "3535", deviceJoinName: "GoControl Smart In-Wall Switch"
        
        //zw:L type:1001 mfr:0307 prod:4447 model:3031 ver:5.06 zwv:4.05 lib:03 cc:5E,86,72,85,59,25,27,73,70,2C,2B,5A,7A role:05 ff:8700 ui:8700
        fingerprint mfr: "0307", prod: "4447", model: "3031", deviceJoinName: "Satco Plug-In Module"
        //zw:L type:1001 mfr:0307 prod:4447 model:3033 ver:5.16 zwv:4.34 lib:03 cc:5E,86,72,5A,85,59,73,25,27,70,2C,2B,5B,7A ccOut:5B role:05 ff:8700 ui:8700
        fingerprint mfr: "0307", prod: "4447", model: "3033", deviceJoinName: "Satco In-Wall Light Switch"
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
