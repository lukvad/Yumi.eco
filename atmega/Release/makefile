################################################################################
# Automatically-generated file. Do not edit!
################################################################################

-include ../makefile.init

RM := rm -rf

# All of the sources participating in the build are defined here
-include sources.mk
-include MK_USART/subdir.mk
-include MK_GSM/subdir.mk
-include subdir.mk
-include objects.mk

ifneq ($(MAKECMDGOALS),clean)
ifneq ($(strip $(ASM_DEPS)),)
-include $(ASM_DEPS)
endif
ifneq ($(strip $(S_DEPS)),)
-include $(S_DEPS)
endif
ifneq ($(strip $(S_UPPER_DEPS)),)
-include $(S_UPPER_DEPS)
endif
ifneq ($(strip $(C_DEPS)),)
-include $(C_DEPS)
endif
endif

-include ../makefile.defs

# Add inputs and outputs from these tool invocations to the build variables 
LSS += \
SIM808-OLD.lss \

FLASH_IMAGE += \
SIM808-OLD.hex \

EEPROM_IMAGE += \
SIM808-OLD.eep \

SIZEDUMMY += \
sizedummy \


# All Target
all: SIM808-OLD.elf secondary-outputs

# Tool invocations
SIM808-OLD.elf: $(OBJS) $(USER_OBJS)
	@echo 'Building target: $@'
	@echo 'Invoking: AVR C Linker'
	avr-gcc -Wl,-Map,SIM808-OLD.map -mmcu=atmega8a -o "SIM808-OLD.elf" $(OBJS) $(USER_OBJS) $(LIBS)
	@echo 'Finished building target: $@'
	@echo ' '

SIM808-OLD.lss: SIM808-OLD.elf
	@echo 'Invoking: AVR Create Extended Listing'
	-avr-objdump -h -S SIM808-OLD.elf  >"SIM808-OLD.lss"
	@echo 'Finished building: $@'
	@echo ' '

SIM808-OLD.hex: SIM808-OLD.elf
	@echo 'Create Flash image (ihex format)'
	-avr-objcopy -R .eeprom -R .fuse -R .lock -R .signature -O ihex SIM808-OLD.elf  "SIM808-OLD.hex"
	@echo 'Finished building: $@'
	@echo ' '

SIM808-OLD.eep: SIM808-OLD.elf
	@echo 'Create eeprom image (ihex format)'
	-avr-objcopy -j .eeprom --no-change-warnings --change-section-lma .eeprom=0 -O ihex SIM808-OLD.elf  "SIM808-OLD.eep"
	@echo 'Finished building: $@'
	@echo ' '

sizedummy: SIM808-OLD.elf
	@echo 'Invoking: Print Size'
	-avr-size --format=avr --mcu=atmega8a SIM808-OLD.elf
	@echo 'Finished building: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) $(FLASH_IMAGE)$(ELFS)$(OBJS)$(ASM_DEPS)$(EEPROM_IMAGE)$(S_DEPS)$(SIZEDUMMY)$(S_UPPER_DEPS)$(LSS)$(C_DEPS) SIM808-OLD.elf
	-@echo ' '

secondary-outputs: $(LSS) $(FLASH_IMAGE) $(EEPROM_IMAGE) $(SIZEDUMMY)

.PHONY: all clean dependents
.SECONDARY:

-include ../makefile.targets
