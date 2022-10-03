@FE8J 080699CA	@{J}
@FE8U 0806769A	@{U}

.thumb
.macro blh to, reg=r3
  ldr \reg, =\to
  mov lr, \reg
  .short 0xf800
.endm


.equ PaletteTable, Table+4

@r6 frameCount

@フレーム 0x11と0x12は薄い色のパレットを出す必要がある
cmp r6, #0x11
beq Startup
cmp r6, #0x12
bne GoBack   @それ以外は何もしない

Startup:
push {r4, r5}

@残念なことにこの関数はthis procsを保存していない。しかたないので取りに行く
@ldr r0,=0x08602488	@efxStoneBG	{J}
ldr r0,=0x085D7E38	@efxStoneBG	{U}
@blh 0x08002dec		@Find6C		{J}
blh 0x08002e9c		@Find6C		{U}
cmp r0, #0x0
beq NotFound

ldr r0, [r0, #0x5c]     @AIS
@blh 0x0805af10   		@GetAISSubjectId	{J}
blh 0x0805a16c   		@GetAISSubjectId	{U}
cmp r0, #0x0
bne LeftSize

RightSide:
@ldr  r0, =0x0203E184	@戦闘アニメで防御側へのRAMポインタ	@{J}
ldr  r0, =0x0203E188	@戦闘アニメで防御側へのRAMポインタ	@{U}
b    JoinGetActor

LeftSize:
@ldr  r0, =0x0203E188	@戦闘アニメで攻撃側へのRAMポインタ	@{J}
ldr  r0, =0x0203E18C	@戦闘アニメで攻撃側へのRAMポインタ	@{U}


JoinGetActor:
ldr  r5, [r0]			@gBattleActor
cmp  r5, #0x0
beq  NotFound

ldr  r4, Table
sub  r4, #0x8

Loop:
add  r4, #0x8

ldr  r0, [r4]
cmp  r0, #0xFF
beq  NotFound

CheckUnit:
ldrb r0, [r4, #0x0] @Table->UnitID
cmp  r0, #0x0
beq  CheckClass

ldr  r1, [r5, #0x0] @gBattleActor->Unit
ldrb r2, [r1, #0x4] @gBattleActor->Unit->ID
cmp  r0, r2
bne  Loop

CheckClass:
ldrb r0, [r4, #0x1] @Table->ClassID
cmp  r0, #0x0
beq  CheckItem

ldr  r1, [r5, #0x4] @gBattleActor->Class
ldrb r2, [r1, #0x4] @gBattleActor->Class->ID
cmp  r0, r2
bne  Loop

CheckItem:
ldrb r0, [r4, #0x2] @Table->ItemID
cmp  r0, #0x0
beq  CheckAffiliation

mov  r1, #0x48
ldrb r1, [r5, r1] @gBattleActor->WeaponID
cmp  r0, r1
bne  Loop

CheckAffiliation:
ldrb r1, [r5, #0xB] @gBattleActor->部隊表ID

ldrb r0, [r4, #0x3] @Table->Aff
cmp  r0, #0x0	@All
beq  Check_Flag

cmp  r0, #0x2 @Enemy
beq  CheckAffiliation_Enemy

cmp  r0, #0x3 @NPC
beq  CheckAffiliation_NPC

CheckAffiliation_Player:
cmp  r1, #0x40
bge  Loop
b    Check_Flag

CheckAffiliation_Enemy:
cmp  r1, #0x80
blt  Loop
b    Check_Flag

CheckAffiliation_NPC:
cmp  r1, #0x40
blt  Loop
cmp  r1, #0x80
ble  Loop
@b    Check_Flag

Check_Flag:
ldrh r0, [r4, #0x4] @Table->Flag
cmp  r0, #0x0 @All
beq  Found

@blh  0x080860d0	@CheckFlag	@{J}
blh  0x08083DA8	@CheckFlag	@{U}
cmp  r0, #0x0
beq  Loop

Found:
ldrh r0, [r4, #0x6] @Table->PaletteID
b    Exit

NotFound:
mov  r0, #0x0        @Default Palette Index 0
@b    Exit

Exit:
pop  {r4, r5}

@palette 0x00 の白の場合は、バニラのルーチンを実行する
@バニラでは2フレームだけ特別に薄い白い霧のパレットがある。
@面倒なので、このパッチでは他の色は用意しません.2フレームだし..
cmp  r0, #0x0
bne  ChoosePalette

@r6が0x11または0x12以外は入れ口で弾いている
@assert r6 is 0x11 or 0x12
cmp r6, #0x11
bne FrameCount12
FrameCount11:
@ldr r0, =0x086F4504	@{J}
ldr r0, =0x086C792C	@{U}
b   SetPalette

FrameCount12:
@ldr r0, =0x086F4524	@{J}
ldr r0, =0x086C794C	@{U}
b   SetPalette

ChoosePalette:
ldr  r3, PaletteTable
lsl  r0, r0, #0x2    @ r0=r0*4
ldr  r0, [r3 , r0]   @ Get Palette Pointer

SetPalette:
mov r1, #0x20
@blh 0x080567e0   @SomePaletteStoringRoutine_SpellAnim2	@{J}
blh 0x08055844   @SomePaletteStoringRoutine_SpellAnim2	@{U}

GoBack:
@ldr r3, =0x08069a0a|1	@{J}
ldr r3, =0x080676da|1	@{U}
bx r3

.align
.ltorg
Table:
@Table
@PaletteTable