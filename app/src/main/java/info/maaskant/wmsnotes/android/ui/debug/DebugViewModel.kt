package info.maaskant.wmsnotes.android.ui.debug

import android.util.Base64
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import info.maaskant.wmsnotes.model.CommandBus
import info.maaskant.wmsnotes.model.CommandExecution
import info.maaskant.wmsnotes.model.CommandOrigin
import info.maaskant.wmsnotes.model.Path
import info.maaskant.wmsnotes.model.note.AddAttachmentCommand
import info.maaskant.wmsnotes.model.note.CreateNoteCommand
import info.maaskant.wmsnotes.model.note.Note
import info.maaskant.wmsnotes.model.note.NoteCommandRequest
import info.maaskant.wmsnotes.utilities.logger
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val commandBus: CommandBus
) : ViewModel() {
    private val logger by logger()

    fun createTestNote(): String? {
        return try {
            val aggId = Note.randomAggId()
            val commandResult = CommandExecution.executeBlocking(
                commandBus = commandBus,
                commandRequest = NoteCommandRequest(
                    aggId = aggId,
                    commands = listOf(
                        CreateNoteCommand(
                            aggId = aggId,
                            path = Path(),
                            title = "",
                            content = content
                        ),
                        AddAttachmentCommand(
                            aggId = aggId,
                            name = attachmentName,
                            content = attachmentContent
                        )
                    ),
                    origin = CommandOrigin.LOCAL
                ),
                timeout = CommandExecution.Duration(500, TimeUnit.MILLISECONDS)
            )
            commandResult.newEvents.first().aggId
        } catch (e: RuntimeException) {
            logger.warn("Could not create new note", e)
            null
        }
    }

    fun createText() : String = UUID.randomUUID().toString()

    companion object {
        private const val attachmentName = "test"
        private val attachmentContent = Base64.decode(
            "iVBORw0KGgoAAAANSUhEUgAAAJQAAADMBAMAAABnz5d9AAAAB3RJTUUH1AQCDScs+jfXBwAAAAlwSFlzAABOIAAATiABFn2Z3gAAAARnQU1BAACxjwv8YQUAAAAwUExURQAAAIAAAACAAICAAAAAgIAAgACAgICAgMDAwP8AAAD/AP//AAAA//8A/wD//////3sfscQAAARVSURBVHja7duLmdsgDABgbaD9t9QGNMQGhCWEEFx67Rc3TXM++w9gHgK7kI5t8KV+CUVo/bhEASTKu683hBD1SsDrxIsifL29/kSofObr9Ezh+5W39z/kK1BOYT4rv8S2RFEl3p9SQTBOYc5OpTBC8VQklqr1DNYCQq2oliiFoCtzmLxNolBmKjaodIBCsA5yl9X8IC9lHEO+a+ihyoV8bbhHUaUauUOlI1SrHnbFh/uL7bLCR05NypDqFx2gkq9P9lJ0hmqdBu5Szo5rTrFmQ456ZVE8LbR3BftcGXmcUo/aS2NrSj1PjVOiSY3bmEKNf1il+qoo0zvMoaCor4oyDeSlni1Eo9BFPVsbhSmqnXillFPAQ1XCoshDtapcGzAGKSVaKKkEXgpziiwKW3k7KClR66lQFuRHKGqhn1rC5KfeswiDEgU4pIhRuEthrkbvj5uputNCfPwMU/JAfS44p9BJ0YxSL7Wawd9EgbYzRFGE6vOipW9OmR0mrFGoULVjBrFvn4L5iNPFUc/MwColB1JJkWegbxbvZUqvL39nUPe8ow8/4O7FaD7NeVLPQLj2qy15yUGV854RI1xD1931o4ti8zV2xLWPZIpNanQQtuynAxSBmb2/MvNSdoowPESpM7EQpU/FLKq1RJQSilm5QZFO8cT0bWxMgUY9qkQX4g8p3kUhh3B01IgihVIqKet8hxRfWa0rMqB95YwiQY1aYOsUB1RZky7UuC3TlEIWxlw91aAhLlJWp1DHjQH1DiN7Ko02sikA3m52qHf+fUtNqR44otgRM6mMuzoF0K7MDLKpSynUtDcmk/IZDupuc14qjSk6R5Vi9FP4Caq/dPM7QkOKBDWtWCOqNDdqbRBjVIuKWc9gWsN6RbKMJguii5RlDdsgC6s5Nc4ijSk5nb/69omkLdCh+HSNOEMKh5RM3x22mCWlUNpsLVODLFoDPSgJrGGtlqZh+NG1kn4OrFhkBUXEZzmP6bQYw8AM1drtWZZZNsHk65yTALK7DyHWVsStzTSh7sRjG3dFLKqN1wrVyp/U8Yt0SVlD5nPd0uZDE5N+EWBp7OmaYqFgi2rNrU4v2zWMUsCplSFfUFCn74Q1w1Gq5GufunqBui/viFJ1d1vcXKtXciQm1h94pCTavhxHKUax2q59WqSUG99hynOzc4lyx2kGNV0w8VLOh37+ZSpQSjrlrY8eKp7Dn6WihSXH33BhaWFtMFlasB201IlJLIvqdCmWLH0+eIwKZtFBedM4Wv1QRp4Y5boX4aTKiAPuhynrEoG2bHgPF/tUjcsXn2I1n975Un5qt179CLX48PCnqIMZ/O/L6kt1VPqVqbKCquUMsg3PUc+llj2quwcYoto9YSPDM+o5nFOcUn91jroCB3+oFZ8dfakv9VGqrQvlv46n2cdUfZjvDJX/d8hLwU0Kbocys52qfIf/9Y4nqFxQh1KVX7BP3cufcKDYD2xf6kvV7Q81hOFV57b+kgAAAABJRU5ErkJggg==",
            Base64.DEFAULT
        )
        private const val content = """# WMS Notes Markdown Test

## H2
### H3
#### H4

Emoji: 😂

*Emphasis*, **bold**, ~~strikethrough~~, `inline code`

```bash
code block
```

[Link](https://github.com/scheleaap/wmsnotes-desktop-and-server)

![Attachment](attachment:test)

![Remote Image](https://github.com/scheleaap/wmsnotes-desktop-and-server/raw/master/graphics/feature-graphic.png)

> Quote
> 
> More quote

* Bullet list
* Item 2

1. Ordered list
1. Item 2


Country | Capital
--- | ---
The Netherlands | Amsterdam
Germany | Berlin
France | Paris

<i>HTML emphasis</i>, <b>HTML bold</b>, <s>HTML strikethrough</s>, <u>HTML underline</u>
"""

    }
}
