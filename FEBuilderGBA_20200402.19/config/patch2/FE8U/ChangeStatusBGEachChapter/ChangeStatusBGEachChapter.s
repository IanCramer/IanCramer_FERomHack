@Call 0x08088594 FE8U
@r0 
@r1 
@r2 
@r3 
@r4 keep
@r5 Table領域と利用する.

.macro blh to, reg=r3
  ldr \reg, =\to
  mov lr, \reg
  .short 0xf800
.endm


.thumb
.org 0x00

push {r5}           @処理的には不要だが、一応、保存しておこう.
                    @r4は必要なデータが入っているので、そもそも変更しない.



ldr  r5,Table
sub  r5,#0x10        @     ループ処理が面倒なので、最初に0x10(16)バイト差し引きます.

Loop:
add  r5,#0x10       @     次のデータへ
ldr  r0,[r5,#0x04]  @     P4:ZIMAGE=Image
cmp  r0,#0x00       @     データのポインタがない場合、終端とみなす.
beq  load_defualt_bg     @データがないので、ディフォルトの背景をロードして終了!

CheckMapID:
ldrb r0,[r5,#0x00]  @     B0:MAPID=MAPID(0xFF=ANY)
cmp  r0,#0xFF       @     ANY MAPID ?
beq  CheckAllegiance

ldr  r2,=#0x202BCF0 @FE8U Chaptor Pointer  (@ChapterData)
ldrb r1,[r2,#0xE]   @     ChapterData->MAPID
cmp  r0,r1
bne  Loop           @     条件不一致なので、次のループへ continue;

CheckAllegiance:
ldrb r3,[r5,#0x01]  @     B1=Allegiance(0xFF=ANY,0x00=Player,0x40=NPC,0x80=Enemy)
cmp  r3,#0xFF       @     ANY Allegiance ?
beq  CheckFlag

bl GetUnitAllegiance @所属の取得 r0,r1,r2 を破壊 結果は r0に戻る
cmp  r3,r0                @r0 == 0x00  Player
                          @r0 == 0x40  NPC
                          @r0 == 0x80  Enemy
bne  Loop           @     条件不一致なので、次のループへ continue;



CheckFlag:
ldrh r0,[r5,#0x02]  @     W2:Flag=Flag(0x00=ANY)
cmp  r0,#0x0        @     ANY Flag ?
beq  Found

blh  0x08083DA8     @FE8U CheckFlag  Flag=r0  Result=r0:bool
cmp	r0,#0x00
beq  Loop           @     条件不一致なので、次のループへ continue;

b    Found          @発見!


GetUnitAllegiance:        @座標からユニットの所属を取得する
                          @Kirbのルーチンを元にした
push {lr}
ldr  r2,=0x202bcc4        @FE8U gCursorMapPosition
ldrh r0,[r2]              @     gCursorMapPosition->xcoord
ldrh r1,[r2,#2]           @     gCursorMapPosition->ycoord

ldr  r2,=0x202e4d8        @FE8U gMapUnit
ldr  r2,[r2]
lsl  r1,#2                @     y*4
add  r1,r2                @     row address
ldr  r1,[r1]
ldrb r0,[r1,r0]

cmp r0,#0
bne CheckAlleg
                          
ldr r0,=0x202be44         @FE8U gActiveUnitIndex
ldrb r0,[r0]
CheckAlleg:
mov r1,#0xc0
and r0,r1

pop {pc}                  @サブルーチンの終わり
                          @r0 == 0x00  Player
                          @r0 == 0x40  NPC
                          @r0 == 0x80  Enemy


Found:              @探索したデータにマッチした。
                    @ユーザが指定した背景をロードする
                    @r5 Table(current)
ldr r0,[r5,#0x04]   @     背景画像
ldr r1,=0xFFFFFFFF  @     FEBuilderGBAの都合 データが0件では困るのでダミーデータがあるよ
cmp r0,r1
beq load_defualt_bg

ldr r1,=0x0600B000  @FE8U 背景をロードするVRAM
blh 0x08012f50      @FE8U UnLZ77Decompress

ldr r0,[r5,#0x0C]   @     背景パレット
mov r1, #0xc0
lsl r1 ,r1 ,#0x1
mov r2, #0x80
blh 0x08000db8      @FE8U CopyToPaletteBuffer 

ldr r0,[r5,#0x08]   @     背景TSA
mov r1 ,r4          @     ロードする領域はr4に書かれている。r4は壊さないように.
blh 0x08012f50      @FE8U UnLZ77Decompress
b Exit

@設定がない場合は、ディフォルトの背景をロードする.
load_defualt_bg:    @ディフォルトの設定をロードする
ldr r0,=0x08088638  @FE8U ディフォルトのステータス画面の背景が書いてあるアドレス
ldr r0,[r0]         @     ポインタ参照することで、リポイントに耐える.
ldr r1,=0x0600B000  @FE8U 背景をロードするVRAM
blh 0x08012f50      @FE8U UnLZ77Decompress

ldr r0,=0x08088640  @FE8U ディフォルトのステータス画面のパレットが書いてあるアドレス
ldr r0,[r0]         @     ポインタ参照することで、リポイントに耐える.
mov r1, #0xc0
lsl r1 ,r1 ,#0x1
mov r2, #0x80
blh 0x08000db8      @FE8U CopyToPaletteBuffer 

ldr r0,=0x08088644  @FE8U TSA
ldr r0,[r0]         @     ポインタ参照することで、リポイントに耐える.
mov r1 ,r4          @     ロードする領域はr4に書かれている。r4は壊さないように.
blh 0x08012f50      @FE8U UnLZ77Decompress

Exit:
pop {r5}
ldr r0,=0x080885B0+1 @FE8U 戻るアドレス
bx  r0

.ltorg
.align
Table:
