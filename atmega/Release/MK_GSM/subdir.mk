################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../MK_GSM/mk_atcmdlist.c \
../MK_GSM/mk_gsm.c 

OBJS += \
./MK_GSM/mk_atcmdlist.o \
./MK_GSM/mk_gsm.o 

C_DEPS += \
./MK_GSM/mk_atcmdlist.d \
./MK_GSM/mk_gsm.d 


# Each subdirectory must supply rules for building sources it contributes
MK_GSM/%.o: ../MK_GSM/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: AVR Compiler'
	avr-gcc -Wall -Os -fpack-struct -fshort-enums -ffunction-sections -fdata-sections -std=gnu99 -funsigned-char -funsigned-bitfields -mmcu=atmega8a -DF_CPU=14745600UL -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -c -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


