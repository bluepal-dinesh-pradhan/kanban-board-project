import { useEditor, EditorContent } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'
import Link from '@tiptap/extension-link'
import Image from '@tiptap/extension-image'
import { Underline } from '@tiptap/extension-underline'
import { TextAlign } from '@tiptap/extension-text-align'
import { Color } from '@tiptap/extension-color'
import { TextStyle } from '@tiptap/extension-text-style'
import { Highlight } from '@tiptap/extension-highlight'
import { useState, useEffect } from 'react'
import { 
  FiBold, 
  FiItalic, 
  FiList, 
  FiLink, 
  FiImage, 
  FiCode, 
  FiType, 
  FiAlignLeft, 
  FiAlignCenter, 
  FiAlignRight, 
  FiRotateCcw, 
  FiRotateCw,
  FiMinus,
  FiBarChart2,
  FiSmile
} from 'react-icons/fi'
import { 
  MdFormatUnderlined, 
  MdStrikethroughS, 
  MdFormatListNumbered, 
  MdFormatQuote, 
  MdFormatColorText, 
  MdFormatListBulleted,
  MdOutlineCheckBox
} from 'react-icons/md'
import { FiEdit3 } from 'react-icons/fi'

const MenuBar = ({ editor }) => {
  if (!editor) {
    return null
  }

  const addImage = () => {
    const url = window.prompt('Enter image URL')
    if (url) {
      editor.chain().focus().setImage({ src: url }).run()
    }
  }

  const setLink = () => {
    const previousUrl = editor.getAttributes('link').href
    const url = window.prompt('URL', previousUrl)

    if (url === null) {
      return
    }

    if (url === '') {
      editor.chain().focus().extendMarkRange('link').unsetLink().run()
      return
    }

    editor.chain().focus().extendMarkRange('link').setLink({ href: url }).run()
  }

  return (
    <div className="flex flex-wrap items-center gap-1 p-1 bg-gray-50 border-b border-gray-200 rounded-t-lg">
      {/* Text Styles */}
      <div className="flex items-center gap-1 px-1 border-r border-gray-300 mr-1">
        <button
          type="button"
          onClick={() => editor.chain().focus().setParagraph().run()}
          className={`p-1.5 rounded hover:bg-gray-200 transition-colors ${editor.isActive('paragraph') ? 'bg-blue-100 text-blue-600' : 'text-gray-600'}`}
          title="Normal Text"
        >
          <FiType className="w-4 h-4" />
        </button>
        <button
          type="button"
          onClick={() => editor.chain().focus().toggleHeading({ level: 1 }).run()}
          className={`p-1.5 rounded hover:bg-gray-200 transition-colors ${editor.isActive('heading', { level: 1 }) ? 'bg-blue-100 text-blue-600' : 'text-gray-600'}`}
          title="Heading 1"
        >
          <span className="font-bold text-xs">H1</span>
        </button>
        <button
          type="button"
          onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}
          className={`p-1.5 rounded hover:bg-gray-200 transition-colors ${editor.isActive('heading', { level: 2 }) ? 'bg-blue-100 text-blue-600' : 'text-gray-600'}`}
          title="Heading 2"
        >
          <span className="font-bold text-xs">H2</span>
        </button>
        <button
          type="button"
          onClick={() => editor.chain().focus().toggleHeading({ level: 3 }).run()}
          className={`p-1.5 rounded hover:bg-gray-200 transition-colors ${editor.isActive('heading', { level: 3 }) ? 'bg-blue-100 text-blue-600' : 'text-gray-600'}`}
          title="Heading 3"
        >
          <span className="font-bold text-xs">H3</span>
        </button>
      </div>

      {/* Basic Formatting */}
      <div className="flex items-center gap-1 px-1 border-r border-gray-300 mr-1">
        <button
          type="button"
          onClick={() => editor.chain().focus().toggleBold().run()}
          disabled={!editor.can().chain().focus().toggleBold().run()}
          className={`p-1.5 rounded hover:bg-gray-200 transition-colors ${editor.isActive('bold') ? 'bg-blue-100 text-blue-600' : 'text-gray-600'}`}
          title="Bold"
        >
          <FiBold className="w-4 h-4" />
        </button>
        <button
          type="button"
          onClick={() => editor.chain().focus().toggleItalic().run()}
          disabled={!editor.can().chain().focus().toggleItalic().run()}
          className={`p-1.5 rounded hover:bg-gray-200 transition-colors ${editor.isActive('italic') ? 'bg-blue-100 text-blue-600' : 'text-gray-600'}`}
          title="Italic"
        >
          <FiItalic className="w-4 h-4" />
        </button>
        <button
          type="button"
          onClick={() => editor.chain().focus().toggleUnderline().run()}
          className={`p-1.5 rounded hover:bg-gray-200 transition-colors ${editor.isActive('underline') ? 'bg-blue-100 text-blue-600' : 'text-gray-600'}`}
          title="Underline"
        >
          <MdFormatUnderlined className="w-4 h-4" />
        </button>
        <button
          type="button"
          onClick={() => editor.chain().focus().toggleStrike().run()}
          disabled={!editor.can().chain().focus().toggleStrike().run()}
          className={`p-1.5 rounded hover:bg-gray-200 transition-colors ${editor.isActive('strike') ? 'bg-blue-100 text-blue-600' : 'text-gray-600'}`}
          title="Strikethrough"
        >
          <MdStrikethroughS className="w-4 h-4" />
        </button>
      </div>

      {/* Colors & Highlight */}
      <div className="flex items-center gap-1 px-1 border-r border-gray-300 mr-1">
        <button
          type="button"
          onClick={() => {
            const color = window.prompt('Enter color (hex or name)')
            if (color) {
              editor.chain().focus().setColor(color).run()
            } else {
              editor.chain().focus().unsetColor().run()
            }
          }}
          className={`p-1.5 rounded hover:bg-gray-200 transition-colors text-gray-600`}
          title="Text Color"
        >
          <MdFormatColorText className="w-4 h-4" />
        </button>
        <button
          type="button"
          onClick={() => editor.chain().focus().toggleHighlight().run()}
          className={`p-1.5 rounded hover:bg-gray-200 transition-colors ${editor.isActive('highlight') ? 'bg-blue-100 text-blue-600' : 'text-gray-600'}`}
          title="Highlight Color"
        >
          <FiEdit3 className="w-4 h-4" />
        </button>
      </div>

      {/* Lists */}
      <div className="flex items-center gap-1 px-1 border-r border-gray-300 mr-1">
        <button
          type="button"
          onClick={() => editor.chain().focus().toggleBulletList().run()}
          className={`p-1.5 rounded hover:bg-gray-200 transition-colors ${editor.isActive('bulletList') ? 'bg-blue-100 text-blue-600' : 'text-gray-600'}`}
          title="Bullet List"
        >
          <MdFormatListBulleted className="w-4 h-4" />
        </button>
        <button
          type="button"
          onClick={() => editor.chain().focus().toggleOrderedList().run()}
          className={`p-1.5 rounded hover:bg-gray-200 transition-colors ${editor.isActive('orderedList') ? 'bg-blue-100 text-blue-600' : 'text-gray-600'}`}
          title="Numbered List"
        >
          <MdFormatListNumbered className="w-4 h-4" />
        </button>
        <button
          type="button"
          onClick={() => editor.chain().focus().toggleCodeBlock().run()}
          className={`p-1.5 rounded hover:bg-gray-200 transition-colors ${editor.isActive('codeBlock') ? 'bg-blue-100 text-blue-600' : 'text-gray-600'}`}
          title="Code Block"
        >
          <FiCode className="w-4 h-4" />
        </button>
        <button
          type="button"
          onClick={() => editor.chain().focus().toggleBlockquote().run()}
          className={`p-1.5 rounded hover:bg-gray-200 transition-colors ${editor.isActive('blockquote') ? 'bg-blue-100 text-blue-600' : 'text-gray-600'}`}
          title="Blockquote"
        >
          <MdFormatQuote className="w-4 h-4" />
        </button>
      </div>

      {/* Alignment */}
      <div className="flex items-center gap-1 px-1 border-r border-gray-300 mr-1">
        <button
          type="button"
          onClick={() => editor.chain().focus().setTextAlign('left').run()}
          className={`p-1.5 rounded hover:bg-gray-200 transition-colors ${editor.isActive({ textAlign: 'left' }) ? 'bg-blue-100 text-blue-600' : 'text-gray-600'}`}
          title="Align Left"
        >
          <FiAlignLeft className="w-4 h-4" />
        </button>
        <button
          type="button"
          onClick={() => editor.chain().focus().setTextAlign('center').run()}
          className={`p-1.5 rounded hover:bg-gray-200 transition-colors ${editor.isActive({ textAlign: 'center' }) ? 'bg-blue-100 text-blue-600' : 'text-gray-600'}`}
          title="Align Center"
        >
          <FiAlignCenter className="w-4 h-4" />
        </button>
        <button
          type="button"
          onClick={() => editor.chain().focus().setTextAlign('right').run()}
          className={`p-1.5 rounded hover:bg-gray-200 transition-colors ${editor.isActive({ textAlign: 'right' }) ? 'bg-blue-100 text-blue-600' : 'text-gray-600'}`}
          title="Align Right"
        >
          <FiAlignRight className="w-4 h-4" />
        </button>
      </div>

      {/* Insert */}
      <div className="flex items-center gap-1 px-1 border-r border-gray-300 mr-1">
        <button
          type="button"
          onClick={setLink}
          className={`p-1.5 rounded hover:bg-gray-200 transition-colors ${editor.isActive('link') ? 'bg-blue-100 text-blue-600' : 'text-gray-600'}`}
          title="Insert Link"
        >
          <FiLink className="w-4 h-4" />
        </button>
        <button
          type="button"
          onClick={addImage}
          className="p-1.5 rounded hover:bg-gray-200 transition-colors text-gray-600"
          title="Insert Image"
        >
          <FiImage className="w-4 h-4" />
        </button>
        <button
          type="button"
          onClick={() => editor.chain().focus().setHorizontalRule().run()}
          className="p-1.5 rounded hover:bg-gray-200 transition-colors text-gray-600"
          title="Horizontal Rule"
        >
          <FiMinus className="w-4 h-4" />
        </button>
      </div>

      {/* History */}
      <div className="flex items-center gap-1 px-1">
        <button
          type="button"
          onClick={() => editor.chain().focus().undo().run()}
          disabled={!editor.can().chain().focus().undo().run()}
          className="p-1.5 rounded hover:bg-gray-200 transition-colors text-gray-600 disabled:opacity-30"
          title="Undo"
        >
          <FiRotateCcw className="w-4 h-4" />
        </button>
        <button
          type="button"
          onClick={() => editor.chain().focus().redo().run()}
          disabled={!editor.can().chain().focus().redo().run()}
          className="p-1.5 rounded hover:bg-gray-200 transition-colors text-gray-600 disabled:opacity-30"
          title="Redo"
        >
          <FiRotateCw className="w-4 h-4" />
        </button>
      </div>
    </div>
  )
}

const RichTextEditor = ({ content, onSave, onCancel, placeholder, editable = false }) => {
  const [isEditing, setIsEditing] = useState(false)
  const [htmlContent, setHtmlContent] = useState(content || '')

  const editor = useEditor({
    extensions: [
      StarterKit.configure({
        bulletList: {
          keepMarks: true,
          keepAttributes: false,
        },
        orderedList: {
          keepMarks: true,
          keepAttributes: false,
        },
      }),
      Placeholder.configure({
        placeholder: placeholder || 'Add a more detailed description...',
      }),
      Link.configure({
        openOnClick: false,
      }),
      Image,
      Underline,
      TextAlign.configure({
        types: ['heading', 'paragraph'],
      }),
      TextStyle,
      Color,
      Highlight,
    ],
    content: content || '',
    editable: isEditing,
    onUpdate({ editor }) {
      setHtmlContent(editor.getHTML())
    },
  })

  useEffect(() => {
    if (editor && content !== editor.getHTML()) {
      editor.commands.setContent(content || '')
      setHtmlContent(content || '')
    }
  }, [content, editor])

  useEffect(() => {
    if (editor) {
      editor.setEditable(isEditing)
    }
  }, [isEditing, editor])

  const handleSave = () => {
    onSave(htmlContent)
    setIsEditing(false)
  }

  const handleCancel = () => {
    editor.commands.setContent(content || '')
    setHtmlContent(content || '')
    setIsEditing(false)
    if (onCancel) onCancel()
  }

  if (!isEditing) {
    return (
      <div className="space-y-2 group">
        <div 
          onClick={() => setIsEditing(true)}
          className={`
            min-h-[150px] p-4 bg-gray-50/50 rounded-lg border border-transparent 
            hover:bg-gray-100/50 hover:border-gray-200 cursor-text transition-all
            prose prose-sm max-w-none
          `}
        >
          {content ? (
            <div dangerouslySetInnerHTML={{ __html: content }} />
          ) : (
            <span className="text-gray-500 italic">{placeholder || 'Add a more detailed description...'}</span>
          )}
        </div>
      </div>
    )
  }

  return (
    <div className="border border-gray-300 rounded-lg overflow-hidden bg-white shadow-sm transition-all focus-within:ring-2 focus-within:ring-blue-100 focus-within:border-blue-400">
      <MenuBar editor={editor} />
      <div className="p-4 bg-white min-h-[150px]">
        <EditorContent editor={editor} />
      </div>
      <div className="px-4 py-3 bg-gray-50 border-t border-gray-100 flex items-center gap-3">
        <button
          onClick={handleSave}
          className="px-4 py-1.5 bg-blue-600 text-white text-sm font-bold rounded-lg hover:bg-blue-700 shadow-md shadow-blue-100 transition-all hover:-translate-y-0.5"
        >
          Save
        </button>
        <button
          onClick={handleCancel}
          className="px-4 py-1.5 text-gray-600 text-sm font-bold hover:bg-gray-200 rounded-lg transition-all"
        >
          Cancel
        </button>
      </div>
      
      {/* Styles for Tiptap */}
      <style dangerouslySetInnerHTML={{ __html: `
        .ProseMirror {
          outline: none;
          min-height: 150px;
        }
        .ProseMirror p.is-editor-empty:first-child::before {
          content: attr(data-placeholder);
          float: left;
          color: #adb5bd;
          pointer-events: none;
          height: 0;
          font-style: italic;
        }
        .ProseMirror ul {
          list-style-type: disc;
          padding-left: 1.5rem;
        }
        .ProseMirror ol {
          list-style-type: decimal;
          padding-left: 1.5rem;
        }
        .ProseMirror blockquote {
          border-left: 3px solid #e2e8f0;
          padding-left: 1rem;
          color: #64748b;
          font-style: italic;
        }
        .ProseMirror pre {
          background: #0f172a;
          color: #f8fafc;
          padding: 1rem;
          border-radius: 0.5rem;
          font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
        }
        .ProseMirror img {
          max-width: 100%;
          border-radius: 0.5rem;
        }
        .ProseMirror hr {
          border: none;
          border-top: 2px solid #f1f5f9;
          margin: 1.5rem 0;
        }
        .ProseMirror a {
          color: #2563eb;
          text-decoration: underline;
          cursor: pointer;
        }
      ` }} />
    </div>
  )
}

export default RichTextEditor
